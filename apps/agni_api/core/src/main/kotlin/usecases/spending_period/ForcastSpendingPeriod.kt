package usecases.spending_period

import usecases.interfaces.IUseCase

import usecases.UseCase
import adapters.dto.QueryFilter
import adapters.repositories.IRepository
import adapters.repositories.QueryExtendBuilder
import adapters.repositories.QueryComparator
import domain.entities.Budget
import domain.entities.Profile
import domain.entities.Provision
import domain.entities.Fund
import domain.entities.ScheduleInvoice
import usecases.dto.ListOutput
import usecases.analystics.ForcastSpending
import usecases.analystics.dto.GetSavingBalanceInput
import usecases.invoices.dto.GetAllInvoiceInput
import usecases.invoices.dto.GetBalanceInput
import usecases.invoices.dto.GetBalanceOutput
import usecases.invoices.dto.GetInvoiceOutput
import usecases.spending_period.dto.ForcastSpendingAchieveItemOutput
import usecases.spending_period.dto.ForcastSpendingPeriodInput
import usecases.spending_period.dto.ForcastSpendingPeriodOutput
import java.time.LocalDate
import java.util.UUID
import domain.enums.InvoiceModuleLinkerType
import domain.enums.InvoiceStatusType
import domain.enums.InvoiceType
import domain.enums.ScheduleInvoiceModuleLinkerType

class ForcastSpendingPeriod(
    private val scheduleInvoiceRepo: IRepository<ScheduleInvoice>,
    private val budgetRepo: IRepository<Budget>,
    private val profileRepo: IRepository<Profile>,
    private val provisionRepo: IRepository<Provision>,
    private val fundRepo: IRepository<Fund>,
    private val getBalance: IUseCase<GetBalanceInput, GetBalanceOutput>,
    private val getSavingBalance: IUseCase<GetSavingBalanceInput, Double>,
    private val getInvoices: IUseCase<GetAllInvoiceInput, ListOutput<GetInvoiceOutput>>
): UseCase<ForcastSpendingPeriodInput, ForcastSpendingPeriodOutput>() {
    override suspend fun process(input: ForcastSpendingPeriodInput): ForcastSpendingPeriodOutput {
        val budgets = budgetRepo.getManyByIds(input.budgetIds.toSet())

        val scheduleInvoiceCondition = QueryExtendBuilder<ScheduleInvoice>()
            .addCondition("isPause", QueryComparator.Equal, false)
            .addCondition("scheduler.date", QueryComparator.LesserOrEquals, input.endDate.atStartOfDay())
        val scheduleInvoices = scheduleInvoiceRepo.getAll(QueryFilter.queryAll(), scheduleInvoiceCondition)

        val invoices = getInvoices.processDirect(GetAllInvoiceInput(
            startDate = input.startDate.atStartOfDay(),
            endDate = input.endDate.atStartOfDay(),
            status = InvoiceStatusType.COMPLETED,
            queryFilter = QueryFilter.queryAll()
        ))

        val currentBalance = getBalance.processDirect(GetBalanceInput(
            startDate = input.startDate.atStartOfDay(),
            endDate = input.endDate.atStartOfDay(),
            removeSystemCategory = true
        ))

        val savingBalance = getSavingBalance.processDirect(GetSavingBalanceInput(
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
        val fixExpenses = getForcastScheduleInvoice(scheduleInvoices.items, InvoiceType.FIXED_COST, invoices.items, fundsById, provisionsById, input.startDate, input.endDate)
        val totalFixedExpenses = fixExpenses.sumOf { it.amount }
        val variableExpenses = getForcastScheduleInvoice(scheduleInvoices.items, InvoiceType.VARIABLE_COST, invoices.items, fundsById, provisionsById, input.startDate, input.endDate)
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
        fundsById: Map<UUID, Fund>,
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