package usecases.analystics

import adapters.dto.QueryFilter
import adapters.repositories.IRepository
import adapters.repositories.QueryExtendBuilder
import adapters.repositories.QueryComparator
import domain.entities.Account
import domain.entities.Budget
import domain.exceptions.NotFoundException
import domain.exceptions.ValidationException
import domain.entities.Profile
import domain.entities.Provision
import domain.entities.Fund
import domain.entities.ScheduleInvoice
import usecases.analystics.dto.ForcastSpendingInput
import usecases.analystics.dto.ForcastSpendingOutput
import usecases.analystics.dto.SavingAdditionalIncomeInput
import usecases.analystics.dto.WantItemOutput
import usecases.interfaces.IUseCase
import usecases.invoices.dto.GetBalanceInput
import usecases.invoices.dto.GetBalanceOutput
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.UUID
import kotlin.math.abs
import domain.enums.AccountType
import domain.enums.InvoiceType
import domain.enums.PeriodType
import domain.enums.ScheduleInvoiceModuleLinkerType

class ForcastSpending(
    private val scheduleInvoiceRepo: IRepository<ScheduleInvoice>,
    private val accountRepo: IRepository<Account>,
    private val budgetRepo: IRepository<Budget>,
    private val profileRepo: IRepository<Profile>,
    private val provisionRepo: IRepository<Provision>,
    private val fundRepo: IRepository<Fund>,
    private val getBalance: IUseCase<GetBalanceInput, GetBalanceOutput>
): IUseCase<ForcastSpendingInput, ForcastSpendingOutput> {
    override fun execAsync(input: ForcastSpendingInput): ForcastSpendingOutput {
        var currentBalance = 0.0
        val accounts = accountRepo.getAll(QueryFilter.queryAll())
        currentBalance = input.overrideAccountsBalance ?: getCurrentBalance(accounts.items)

        val budgets = budgetRepo.getManyByIds(input.budgetIds.toSet())

        val budgetExpenses = getBudgetExpense(budgets, input.startDate, input.endDate, getBalance)
        val totalBudgetExpense = budgetExpenses.sumOf { it.remaining }

        val scheduleInvoiceCondition = QueryExtendBuilder<ScheduleInvoice>()
            .addCondition("isPause", QueryComparator.Equal, false)
            .addCondition("scheduler.date", QueryComparator.LesserOrEquals, input.endDate.atStartOfDay())
        val scheduleInvoices = scheduleInvoiceRepo.getAll(QueryFilter.queryAll(), scheduleInvoiceCondition)

        val provisionIds = scheduleInvoices.items.filter { it.moduleLinker?.module == ScheduleInvoiceModuleLinkerType.PROVISION  }.mapNotNull { it.moduleLinker?.sourceId }
        val provisions = provisionRepo.getManyByIds(provisionIds.toSet()).filter { it.paymentInfo != null && it.paymentInfo!!.endDate >= LocalDate.now() }
        val fundIds = provisions.mapNotNull { it.fundAmortizationId }
        val funds = fundRepo.getManyByIds(fundIds.toSet())

        val provisionsById = provisions.associateBy { it.id }
        val fundsById = funds.associateBy { it.id }

        val income = getScheduleTotal(scheduleInvoices.items, fundsById, provisionsById, InvoiceType.INCOME, input.startDate, input.endDate)
        val fixExpense = getScheduleTotal(scheduleInvoices.items, fundsById, provisionsById, InvoiceType.FIXED_COST, input.startDate, input.endDate)
        val variableExpense = getScheduleTotal(scheduleInvoices.items, fundsById, provisionsById, InvoiceType.VARIABLE_COST, input.startDate, input.endDate)

        val freezeBalanceToRemove = getBalance.execAsync(GetBalanceInput(
            isFreeze = true,
            startDate = input.startDate.atStartOfDay(),
            endDate = input.endDate.atStartOfDay()
        ))
        val freezeExpense = getPlanFreezeExpense(scheduleInvoices.items, input.startDate, input.endDate)

        val profiles = profileRepo.getAll(QueryFilter.queryAll())
        var savingRate = profiles.items.first().savingPercentage ?: 0.0
        if (input.savingRate != null)
            savingRate = input.savingRate

        val saving = income * (savingRate/100.00)

        val additionalIncome = getAdditionalSavingAmount(input.savingAdditionalIncome, accounts.items)

        val totalIncome = income + currentBalance + additionalIncome + abs(freezeBalanceToRemove.balance)
        val totalExpense = fixExpense + variableExpense + freezeExpense + totalBudgetExpense + saving

        val remain = totalIncome - totalExpense
        val margeRemain = (remain * (savingRate/100.0))
        val validItems = input.wantItems.filter { it.amount <= (remain - margeRemain) }

        val acceptedItems = selectItemsWantRecursive(remain - margeRemain, validItems)

        return ForcastSpendingOutput(
            remainAmount = remain,
            totalExpectedIncome = totalIncome,
            totalExpectedExpense = totalExpense,
            expectedIncome = income,
            expectedFixExpense = fixExpense,
            expectedVariableExpense = variableExpense,
            expectedPlanFreezeExpense = freezeExpense,
            expectedBudgetExpense = totalBudgetExpense,
            expectedSaving = saving,
            itemsApproved = acceptedItems,
            itemsRejected = acceptedItems.filter { !validItems.contains(it) }
        )
    }

    private fun selectItemsWantRecursive(remain: Double, acceptedItems: List<WantItemOutput>): List<WantItemOutput> {
        val repartition = remain / acceptedItems.size
        val validItems = mutableListOf<WantItemOutput>()
        val rejectedItems = mutableListOf<WantItemOutput>()
        for (item in acceptedItems) {
            if (item.amount <= repartition) {
                validItems.add(item)
            } else {
                rejectedItems.add(item)
            }
        }

        if (validItems.isNotEmpty())
            validItems += selectItemsWantRecursive(remain - validItems.sumOf { it.amount }, rejectedItems)

        return validItems
    }

    private fun getAdditionalSavingAmount(
        additionalAccounts: List<SavingAdditionalIncomeInput>,
        accounts: List<Account>
    ): Double {
        val savingAccountsById = accounts
            .filter { it.detail.getType() == AccountType.SAVING }
            .associateBy { it.id }

        val missingAccountIds = additionalAccounts
            .map { it.savingAccountId }
            .filterNot { savingAccountsById.containsKey(it) }

        if (missingAccountIds.isNotEmpty()) {
            throw NotFoundException.ManyEntities(missingAccountIds, "account")
        }

        for (additional in additionalAccounts) {
            val account = savingAccountsById.getValue(additional.savingAccountId)
            if (additional.amount > account.balance) {
                throw ValidationException.ForcastAdditionalSavingAmountMustLessThanBalance(account.balance, additional.amount)
            }
        }

        return additionalAccounts.sumOf { it.amount }
    }

    private fun getScheduleTotal(scheduleInvoices: List<ScheduleInvoice>, fundsById: Map<UUID, Fund>, provisionsById: Map<UUID, Provision>, invoiceType: InvoiceType, startDate: LocalDate, endDate: LocalDate): Double {
        var total = 0.0
        var scheduleInvoices = scheduleInvoices.filter { it.type == invoiceType }
        scheduleInvoices = scheduleInvoices.filter {
            scheduleInvoice -> scheduleInvoice.scheduler.upgradeDate(startDate.atStartOfDay()) >= startDate.atStartOfDay()
        }

        for (schedule in scheduleInvoices) {
            var scheduleStartDate = schedule.scheduler.date
            if (scheduleStartDate < startDate.atStartOfDay())
                scheduleStartDate = schedule.scheduler.upgradeDate(startDate.atStartOfDay())
            val occurrence = schedule.scheduler.repeater?.computeOccurrences(scheduleStartDate.toLocalDate(), endDate) ?: 1

            val provision = provisionsById[schedule.moduleLinker?.sourceId]
            val fund = provision?.fundAmortizationId?.let(fundsById::get)
            var coveredAmount = 0.0

            if (fund != null) {
                val paymentAmount = provision.paymentInfo?.paymentAmount ?: 0.0
                val installmentCount =
                    provision.paymentInfo?.scheduler?.repeater
                        ?.computeOccurrences(scheduleStartDate.toLocalDate(), endDate)
                        ?: occurrence

                val totalInstallment = installmentCount * paymentAmount
                coveredAmount = minOf(fund.balance, totalInstallment)
            }

            total += schedule.amount * occurrence
            total -= coveredAmount
        }

        return total
    }

    private fun getCurrentBalance(accounts: List<Account>): Double {
        return accounts.filter {
            !listOf(AccountType.SAVING, AccountType.BROKING).contains(it.detail.getType())
        }.sumOf { it.balance }
    }

    private fun getPlanFreezeExpense(scheduleInvoices: List<ScheduleInvoice>, startDate: LocalDate, endDate: LocalDate): Double {
        var total = 0.0
        for (schedule in scheduleInvoices.filter { it.isFreeze && it.getFreezeEndDate() > endDate } ) {
            val occurrence = schedule.scheduler.repeater?.computeOccurrences(startDate, endDate) ?: 1
            total += schedule.amount * occurrence
        }

        return total
    }

    companion object {
        data class ForcastBudget(
            val budgetId: UUID,
            val title: String,
            val target: Double,
            val balance: Double,
            val remaining: Double
        )
        fun getBudgetExpense(
            budgets: List<Budget>,
            startDate: LocalDate,
            endDate: LocalDate,
            getBalance: IUseCase<GetBalanceInput, GetBalanceOutput>
        ): List<ForcastBudget> {
            val totals = mutableListOf<ForcastBudget>()

            for (budget in budgets) {
                if (
                    budget.scheduler.upgradeDate(startDate.atStartOfDay()) <= startDate.atStartOfDay() ||
                    budget.isArchived) {
                    continue
                }
                val spend = getBalance.execAsync(GetBalanceInput(
                    startDate = startDate.atStartOfDay(),
                    endDate = endDate.atStartOfDay(),
                    budgetIds = setOf(budget.id)
                )).spend

                val currentBalance = abs(spend)

                val debutCountDate = if (budget.scheduler.date.toLocalDate() >= startDate) {
                    startDate
                } else {
                    budget.scheduler.date.toLocalDate()
                }

                val numberOfDayBudget = ChronoUnit.DAYS.between(debutCountDate, endDate).toDouble()
                val repeater = budget.scheduler.repeater

                val target = if (repeater != null && repeater.interval > 0) {
                    val periodDays = when (repeater.period) {
                        PeriodType.DAY -> 1.0
                        PeriodType.WEEK -> 7.0 * repeater.interval
                        PeriodType.MONTH -> 30.4167 * repeater.interval
                        PeriodType.YEAR -> 365.0 * repeater.interval
                    }

                    budget.target * (numberOfDayBudget / periodDays)
                } else {
                    budget.target
                }

                totals.add(ForcastBudget(
                    budgetId = budget.id,
                    title = budget.title,
                    target = target,
                    balance = currentBalance,
                    remaining = (target - currentBalance)
                ))
            }

            return totals
        }
    }
}