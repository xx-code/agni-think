package dev.auguste.agni_api.core.usecases.spending_period

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.adapters.repositories.QueryExtendBuilder
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryComparator
import dev.auguste.agni_api.core.entities.Budget
import dev.auguste.agni_api.core.entities.Profile
import dev.auguste.agni_api.core.entities.Provision
import dev.auguste.agni_api.core.entities.SavingGoal
import dev.auguste.agni_api.core.entities.ScheduleInvoice
import dev.auguste.agni_api.core.entities.enums.InvoiceModuleLinkerType
import dev.auguste.agni_api.core.entities.enums.InvoiceStatusType
import dev.auguste.agni_api.core.entities.enums.InvoiceType
import dev.auguste.agni_api.core.entities.enums.ScheduleInvoiceModuleLinkerType
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.analystics.ForcastSpending
import dev.auguste.agni_api.core.usecases.analystics.dto.GetSavingBalanceInput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.invoices.dto.GetAllInvoiceInput
import dev.auguste.agni_api.core.usecases.invoices.dto.GetBalanceInput
import dev.auguste.agni_api.core.usecases.invoices.dto.GetBalanceOutput
import dev.auguste.agni_api.core.usecases.invoices.dto.GetInvoiceOutput
import dev.auguste.agni_api.core.usecases.spending_period.dto.ForcastSpendingAchieveItemOutput
import dev.auguste.agni_api.core.usecases.spending_period.dto.ForcastSpendingPeriodInput
import dev.auguste.agni_api.core.usecases.spending_period.dto.ForcastSpendingPeriodOutput
import java.time.LocalDate
import java.util.UUID

class ForcastSpendingPeriod(
    private val scheduleInvoiceRepo: IRepository<ScheduleInvoice>,
    private val budgetRepo: IRepository<Budget>,
    private val profileRepo: IRepository<Profile>,
    private val provisionRepo: IRepository<Provision>,
    private val fundRepo: IRepository<SavingGoal>,
    private val getBalance: IUseCase<GetBalanceInput, GetBalanceOutput>,
    private val getSavingBalance: IUseCase<GetSavingBalanceInput, Double>,
    private val getInvoices: IUseCase<GetAllInvoiceInput, ListOutput<GetInvoiceOutput>>
): IUseCase<ForcastSpendingPeriodInput, ForcastSpendingPeriodOutput> {
    override fun execAsync(input: ForcastSpendingPeriodInput): ForcastSpendingPeriodOutput {
        val budgets = budgetRepo.getManyByIds(input.budgetIds.toSet())

        val scheduleInvoiceCondition = QueryExtendBuilder<ScheduleInvoice>()
            .addCondition("isPause", QueryComparator.Equal, false)
            .addCondition("scheduler.date", QueryComparator.LesserOrEquals, input.endDate.atStartOfDay())
        val scheduleInvoices = scheduleInvoiceRepo.getAll(QueryFilter.queryAll(), scheduleInvoiceCondition)

        val invoices = getInvoices.execAsync(GetAllInvoiceInput(
            startDate = input.startDate.atStartOfDay(),
            endDate = input.endDate.atStartOfDay(),
            status = InvoiceStatusType.COMPLETED,
            queryFilter = QueryFilter.queryAll()
        ))

        val currentBalance = getBalance.execAsync(GetBalanceInput(
            startDate = input.startDate.atStartOfDay(),
            endDate = input.endDate.atStartOfDay(),
            removeSystemCategory = true
        ))

        val savingBalance = getSavingBalance.execAsync(GetSavingBalanceInput(
            startDate = input.startDate.atStartOfDay(),
            endDate = input.endDate.atStartOfDay()
        ))

        val budgetExpenses = ForcastSpending.getBudgetExpense(budgets, input.startDate, input.endDate, getBalance)
        val totalBudgetExpense = budgetExpenses.sumOf { it.target }
        val totalBudgetBalance = budgetExpenses.sumOf { it.balance }


        val provisionIds = scheduleInvoices.items.filter { it.moduleLinker?.module == ScheduleInvoiceModuleLinkerType.PROVISION  }.mapNotNull { it.moduleLinker?.sourceId }
        val provisions = provisionRepo.getManyByIds(provisionIds.toSet()).filter { it.paymentInfo != null && it.paymentInfo!!.endDate >= LocalDate.now() }
        val fundIds = provisions.mapNotNull { it.fundAmortizationId }
        val funds = fundRepo.getManyByIds(fundIds.toSet())

        val provisionsById = provisions.associateBy { it.id }
        val fundsById = funds.associateBy { it.id }

        val incomes = getForcastScheduleInvoice(scheduleInvoices.items, InvoiceType.INCOME, invoices.items, fundsById, provisionsById, input.startDate, input.endDate)
        val totalIncome = incomes.sumOf { it.amount }
        val fixExpenses = getForcastScheduleInvoice(scheduleInvoices.items, InvoiceType.FIXEDCOST, invoices.items, fundsById, provisionsById, input.startDate, input.endDate)
        val totalFixedExpenses = fixExpenses.sumOf { it.amount }
        val variableExpenses = getForcastScheduleInvoice(scheduleInvoices.items, InvoiceType.VARIABLECOST, invoices.items, fundsById, provisionsById, input.startDate, input.endDate)
        val totalVariableExpenses = variableExpenses.sumOf { it.amount }

        val profiles = profileRepo.getAll(QueryFilter.queryAll())
        var savingRate = profiles.items.firstOrNull()?.savingPercentage ?: 0.0
        if (input.savingRate != null)
            savingRate = input.savingRate

        val expectedSaving = totalIncome * (savingRate/100)
        val totalExpectedSpending = (totalFixedExpenses + totalVariableExpenses + expectedSaving + totalBudgetExpense)
        val expectedRemain = totalIncome - totalExpectedSpending


        return ForcastSpendingPeriodOutput(
            expectedRemainAmount = expectedRemain,
            currentRemainAmount = currentBalance.balance,
            totalExpectedIncome = totalIncome,
            totalExpectedExpense = totalExpectedSpending,
            expectedFixExpense = totalFixedExpenses,
            expectedVariableExpense = totalVariableExpenses,
            expectedBudgetExpense = totalBudgetExpense,
            currentBudgetExpense = totalBudgetBalance,
            expectedSaving = expectedSaving,
            currentSaving = savingBalance,
            currentIncome = currentBalance.income,
            incomeItems = incomes,
            fixExpenseItems = fixExpenses,
            variableExpenseItems = variableExpenses,
            achievedWishedItems = listOf()
        )
    }

    private fun getForcastScheduleInvoice(
        scheduleInvoices: List<ScheduleInvoice>,
        invoiceType: InvoiceType,
        invoices: List<GetInvoiceOutput>,
        fundsById: Map<UUID, SavingGoal>,
        provisionsById: Map<UUID, Provision>,
        startDate: LocalDate, endDate: LocalDate,
    ): List<ForcastSpendingAchieveItemOutput> {
        var scheduleInvoices = scheduleInvoices.filter { it.type == invoiceType }
        scheduleInvoices = scheduleInvoices.filter {
            scheduleInvoice -> scheduleInvoice.scheduler.upgradeDate(startDate.atStartOfDay()) >= startDate.atStartOfDay()
        }

        val filterInvoices = invoices.filter { it.type == InvoiceType.INCOME.value && it.moduleLinkers.find { linker -> linker.module == InvoiceModuleLinkerType.SCHEDULE_INVOICE.value } != null }

        return scheduleInvoices.map { schedule ->
            var scheduleStartDate = schedule.scheduler.date
            if (scheduleStartDate < startDate.atStartOfDay())
                scheduleStartDate = schedule.scheduler.upgradeDate(startDate.atStartOfDay())

            val occurrence = schedule.scheduler.repeater?.computeOccurrences(scheduleStartDate.toLocalDate(), endDate) ?: 1
            val invoice = filterInvoices.filter { schedule.id == it.moduleLinkers.first({ linker -> linker.module == InvoiceModuleLinkerType.SCHEDULE_INVOICE.value }).sourceId  }
            val currentAmount = invoice.sumOf { it.total }

            val totalAmount = occurrence * schedule.amount

            val adjustedAmount = when {
                schedule.moduleLinker?.module != ScheduleInvoiceModuleLinkerType.PROVISION ->  totalAmount
                else -> {
                    val provision = provisionsById[schedule.moduleLinker?.sourceId]
                    val fund = provision?.fundAmortizationId?.let(fundsById::get)
                    val payment = provision?.paymentInfo

                    if (fund == null || payment == null) {
                        totalAmount
                    } else {
                        val installmentCount =
                            payment.scheduler.repeater
                                ?.computeOccurrences(scheduleStartDate.toLocalDate(), endDate)
                                ?: occurrence

                        val payable = installmentCount * payment.paymentAmount
                        val covered = minOf(fund.balance, payable)

                        totalAmount - covered
                    }
                }
            }

            ForcastSpendingAchieveItemOutput(
                description = schedule.title,
                amount = adjustedAmount,
                validAmount = invoice.sumOf { it.total },
                isAchieved =  currentAmount >= adjustedAmount ,
            )
        }
    }
}