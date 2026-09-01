package dev.auguste.agni_api.core.usecases.analystics

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.adapters.repositories.query_extend.ComparatorType
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryBudgetExtend
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryDateComparator
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryScheduleInvoiceExtend
import dev.auguste.agni_api.core.entities.Account
import dev.auguste.agni_api.core.entities.Budget
import dev.auguste.agni_api.core.entities.Profile
import dev.auguste.agni_api.core.entities.ScheduleInvoice
import dev.auguste.agni_api.core.entities.enums.AccountType
import dev.auguste.agni_api.core.entities.enums.InvoiceType
import dev.auguste.agni_api.core.usecases.analystics.dto.ForcastSpendingInput
import dev.auguste.agni_api.core.usecases.analystics.dto.ForcastSpendingOutput
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
        if (input.overrideAccountsBalance != null) {
            currentBalance = input.overrideAccountsBalance
        } else {
            val accounts = accountRepo.getAll(QueryFilter.queryAll())
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

        val totalIncome = income + currentBalance
        val totalExpense = fixExpense + variableExpense + freezeExpense + budgetExpense + saving

        return ForcastSpendingOutput(
            remainAmount = totalIncome - totalExpense,
            totalExpectedIncome = totalIncome,
            totalExpectedExpense = totalExpense,
            expectedIncome = income,
            expectedFixExpense = fixExpense,
            expectedVariableExpense = variableExpense,
            expectedPlanFreezeExpense = freezeExpense,
            expectedBudgetExpense = budgetExpense,
            expectedSaving = saving,
            itemsApproved = listOf(),
            itemsRejected = listOf()
        )
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