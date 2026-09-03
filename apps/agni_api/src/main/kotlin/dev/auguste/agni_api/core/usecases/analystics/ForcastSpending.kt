package dev.auguste.agni_api.core.usecases.analystics

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryComparator
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryDateComparator
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryScheduleInvoiceExtend
import dev.auguste.agni_api.core.entities.Account
import dev.auguste.agni_api.core.entities.Budget
import dev.auguste.agni_api.core.entities.DomainException
import dev.auguste.agni_api.core.entities.Profile
import dev.auguste.agni_api.core.entities.ScheduleInvoice
import dev.auguste.agni_api.core.entities.enums.AccountType
import dev.auguste.agni_api.core.entities.enums.InvoiceType
import dev.auguste.agni_api.core.entities.enums.PeriodType
import dev.auguste.agni_api.core.usecases.analystics.dto.ForcastSpendingInput
import dev.auguste.agni_api.core.usecases.analystics.dto.ForcastSpendingOutput
import dev.auguste.agni_api.core.usecases.analystics.dto.SavingAdditionalIncomeInput
import dev.auguste.agni_api.core.usecases.analystics.dto.WantItemOutput
import dev.auguste.agni_api.core.usecases.budgets.dto.GetBudgetOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.invoices.dto.GetBalanceInput
import dev.auguste.agni_api.core.usecases.invoices.dto.GetBalanceOutput
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.UUID
import kotlin.math.abs

class ForcastSpending(
    private val scheduleInvoiceRepo: IRepository<ScheduleInvoice>,
    private val accountRepo: IRepository<Account>,
    private val budgetRepo: IRepository<Budget>,
    private val profileRepo: IRepository<Profile>,
    private val getBudget: IUseCase<UUID, GetBudgetOutput>,
    private val getBalance: IUseCase<GetBalanceInput, GetBalanceOutput>
): IUseCase<ForcastSpendingInput, ForcastSpendingOutput> {
    override fun execAsync(input: ForcastSpendingInput): ForcastSpendingOutput {
        var currentBalance = 0.0
        val accounts = accountRepo.getAll(QueryFilter.queryAll())
        if (input.overrideAccountsBalance != null) {
            currentBalance = input.overrideAccountsBalance
        } else {
            currentBalance = getCurrentBalance(accounts.items)
        }

        val budgets = budgetRepo.getManyByIds(input.budgetIds.toSet())

        val budgetExpense = getBudgetExpense(budgets, input.startDate, input.endDate)

        val scheduleInvoices = scheduleInvoiceRepo.getAll(QueryFilter.queryAll(), QueryScheduleInvoiceExtend(
            comparatorDueDate = QueryDateComparator(
                input.endDate.atStartOfDay(),
                comparator = QueryComparator.LesserOrEquals,
            )
        ))

        val income = getIncome(scheduleInvoices.items.filter{ it.scheduler.date.toLocalDate() >= input.startDate}, input.startDate, input.endDate)
        val fixExpense = getFixExpense(scheduleInvoices.items.filter {  it.scheduler.date.toLocalDate() >= input.startDate} , input.startDate, input.endDate)
        val variableExpense = getVariableExpense(scheduleInvoices.items.filter {  it.scheduler.date.toLocalDate() >= input.startDate} , input.startDate, input.endDate)

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
        val totalExpense = fixExpense + variableExpense + freezeExpense + budgetExpense + saving

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
            expectedBudgetExpense = budgetExpense,
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
            throw DomainException.NotFound.ManyAccounts(missingAccountIds)
        }

        for (additional in additionalAccounts) {
            val account = savingAccountsById.getValue(additional.savingAccountId)
            if (additional.amount > account.balance) {
                throw DomainException.BusinessLogic.ForcastAdditionalSavingAmountMustLessThanBalance(account.balance, additional.amount)
            }
        }

        return additionalAccounts.sumOf { it.amount }
    }

    private fun getIncome(scheduleInvoices: List<ScheduleInvoice>, startDate: LocalDate, endDate: LocalDate): Double {
        var totalIncome = 0.0
        for (schedule in scheduleInvoices.filter { it.type == InvoiceType.INCOME }) {
            val occurrence = schedule.scheduler.repeater?.computeOccurrences(startDate, endDate) ?: 1
            totalIncome += schedule.amount * occurrence
        }

        return totalIncome
    }

    private fun getCurrentBalance(accounts: List<Account>): Double {
        return accounts.filter {
            !listOf(AccountType.SAVING, AccountType.BROKING).contains(it.detail.getType())
        }.sumOf { it.balance }
    }

    private fun getFixExpense(scheduleInvoices: List<ScheduleInvoice>, startDate: LocalDate, endDate: LocalDate): Double {
        var total = 0.0
        for (schedule in scheduleInvoices.filter { it.type == InvoiceType.FIXEDCOST } ) {
            val occurrence = schedule.scheduler.repeater?.computeOccurrences(startDate, endDate) ?: 1
            total += schedule.amount * occurrence
        }

        return total
    }

    private fun getVariableExpense(scheduleInvoices: List<ScheduleInvoice>, startDate: LocalDate, endDate: LocalDate): Double {
        var total = 0.0
        for (schedule in scheduleInvoices.filter { it.type == InvoiceType.VARIABLECOST } ) {
            val occurrence = schedule.scheduler.repeater?.computeOccurrences(startDate, endDate) ?: 1
            total += schedule.amount * occurrence
        }

        return total
    }

    private fun getPlanFreezeExpense(scheduleInvoices: List<ScheduleInvoice>, startDate: LocalDate, endDate: LocalDate): Double {
        var total = 0.0
        for (schedule in scheduleInvoices.filter { it.isFreeze && it.getFreezeEndDate() > endDate } ) {
            val occurrence = schedule.scheduler.repeater?.computeOccurrences(startDate, endDate) ?: 1
            total += schedule.amount * occurrence
        }

        return total
    }

    private fun getBudgetExpense(
        budgets: List<Budget>,
        startDate: LocalDate,
        endDate: LocalDate
    ): Double {
        var total = 0.0

        for (budget in budgets) {
            val spend =getBalance.execAsync(GetBalanceInput(
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

            val budgetTotal = (target - currentBalance)
            if (budgetTotal > 0.0)
                total += budgetTotal

        }

        return total
    }
}