package dev.auguste.agni_api.core.usecases.analystics

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.adapters.repositories.query_extend.ComparatorType
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryBudgetExtend
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryDateComparator
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryScheduleInvoiceExtend
import dev.auguste.agni_api.core.entities.Account
import dev.auguste.agni_api.core.entities.Budget
import dev.auguste.agni_api.core.entities.DomainException
import dev.auguste.agni_api.core.entities.Profile
import dev.auguste.agni_api.core.entities.ScheduleInvoice
import dev.auguste.agni_api.core.entities.enums.AccountType
import dev.auguste.agni_api.core.entities.enums.InvoiceType
import dev.auguste.agni_api.core.usecases.analystics.dto.ForcastSpendingInput
import dev.auguste.agni_api.core.usecases.analystics.dto.ForcastSpendingOutput
import dev.auguste.agni_api.core.usecases.analystics.dto.SavingAdditionalIncomeInput
import dev.auguste.agni_api.core.usecases.analystics.dto.WantItemOutput
import dev.auguste.agni_api.core.usecases.budgets.dto.GetBudgetOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import java.time.LocalDate
import java.util.UUID

class ForcastSpending(
    private val scheduleInvoiceRepo: IRepository<ScheduleInvoice>,
    private val accountRepo: IRepository<Account>,
    private val budgetRepo: IRepository<Budget>,
    private val profileRepo: IRepository<Profile>,
    private val getBudget: IUseCase<UUID, GetBudgetOutput>,
): IUseCase<ForcastSpendingInput, ForcastSpendingOutput> {
    override fun execAsync(input: ForcastSpendingInput): ForcastSpendingOutput {
        var currentBalance = 0.0
        val accounts = accountRepo.getAll(QueryFilter.queryAll())
        if (input.overrideAccountsBalance != null) {
            currentBalance = input.overrideAccountsBalance
        } else {
            currentBalance = getCurrentBalance(accounts.items)
        }

        val budgets = budgetRepo.getAll(QueryFilter.queryAll(),
            QueryBudgetExtend(QueryDateComparator(
                input.endDate.atStartOfDay(),
                ComparatorType.LesserOrEquals
            )))

        val budgetExpense = getBudgetExpense(budgets.items.filter { !it.isArchived  })

        val scheduleInvoices = scheduleInvoiceRepo.getAll(QueryFilter.queryAll(), QueryScheduleInvoiceExtend(
            comparatorDueDate = QueryDateComparator(
                input.endDate.atStartOfDay(),
                ComparatorType.LesserOrEquals
            )
        ))

        val income = getIncome(scheduleInvoices.items)
        val fixExpense = getFixExpense(scheduleInvoices.items)
        val variableExpense = getVariableExpense(scheduleInvoices.items)
        val freezeExpense = getPlanFreezeExpense(scheduleInvoices.items, input.endDate)

        val profiles = profileRepo.getAll(QueryFilter.queryAll())
        var savingRate = profiles.items.first().savingPercentage ?: 0.0
        if (input.savingRate != null)
            savingRate = input.savingRate

        val saving = income * (savingRate/100.00)

        val additionalIncome = getAdditionalSavingAmount(input.savingAdditionalIncome, accounts.items)

        val totalIncome = income + currentBalance + additionalIncome
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

    private fun getIncome(scheduleInvoices: List<ScheduleInvoice>): Double {
        return scheduleInvoices.filter {
            it.type == InvoiceType.INCOME
        }.sumOf { it.amount }
    }

    private fun getCurrentBalance(accounts: List<Account>): Double {
        return accounts.filter {
            !listOf(AccountType.SAVING, AccountType.BROKING).contains(it.detail.getType())
        }.sumOf { it.balance }
    }

    private fun getFixExpense(scheduleInvoices: List<ScheduleInvoice>): Double {
        return scheduleInvoices.filter {
            it.type == InvoiceType.FIXEDCOST
        }.sumOf { it.amount }
    }

    private fun getVariableExpense(scheduleInvoices: List<ScheduleInvoice>): Double {
        return scheduleInvoices.filter {
            it.type == InvoiceType.VARIABLECOST
        }.sumOf { it.amount }
    }

    private fun getPlanFreezeExpense(scheduleInvoices: List<ScheduleInvoice>, endDate: LocalDate): Double {
        return scheduleInvoices.filter {
            it.isFreeze && it.getFreezeEndDate() > endDate
        }.sumOf { it.amount }
    }

    private fun getBudgetExpense(budgets: List<Budget>): Double {
        var total = 0.0
        for (budget in budgets) {
            val resBudget = getBudget.execAsync(budget.id)
            total += resBudget.target - resBudget.currentBalance
        }
        return total
    }
}