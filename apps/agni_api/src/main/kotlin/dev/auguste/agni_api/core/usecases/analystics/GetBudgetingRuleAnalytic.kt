package dev.auguste.agni_api.core.usecases.analystics

import dev.auguste.agni_api.core.SAVING_CATEGORY_ID
import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Account
import dev.auguste.agni_api.core.entities.enums.AccountType
import dev.auguste.agni_api.core.entities.enums.InvoiceType
import dev.auguste.agni_api.core.entities.utils.LocalDateTimeRange
import dev.auguste.agni_api.core.usecases.analystics.dto.GetBudgetingRuleAnalyticInput
import dev.auguste.agni_api.core.usecases.analystics.dto.GetBudgetingRuleAnalyticOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.invoices.dto.GetBalanceInput
import dev.auguste.agni_api.core.usecases.invoices.dto.GetBalanceOutput
import java.time.LocalDateTime

class GetBudgetingRuleAnalytic(
    private val getBalance: IUseCase<GetBalanceInput, GetBalanceOutput>,
    private val accountRepo: IRepository<Account>
) : IUseCase<GetBudgetingRuleAnalyticInput, GetBudgetingRuleAnalyticOutput> {
    override fun execAsync(input: GetBudgetingRuleAnalyticInput): GetBudgetingRuleAnalyticOutput {
        val range = if (input.startDate != null && input.endDate != null) {
            LocalDateTimeRange(input.startDate, input.endDate)
        } else {
            LocalDateTimeRange.fromPeriod(input.period!!, input.interval)
        }

        val periodBalance = getBalance.execAsync(GetBalanceInput(
            startDate = range.start,
            endDate = range.end,
        ))

        val fixedCostBalance = getBalance.execAsync(GetBalanceInput(
            types = setOf(InvoiceType.FIXEDCOST),
            startDate = range.start,
            endDate = range.end,
        ))

        val variableCostBalance = getBalance.execAsync(GetBalanceInput(
            types = setOf(InvoiceType.VARIABLECOST),
            startDate = range.start,
            endDate = range.end
        ))

        val savingBalance = getSavingBalance(range.start, range.end)

        if (periodBalance.income <= 0) {
            return GetBudgetingRuleAnalyticOutput(
                ratioSaving = 0.0,
                ratioFixCost = 0.0,
                ratioVariableCost = 0.0,
                fixCost = fixedCostBalance.spend,
                variableCost = variableCostBalance.spend,
                savingAmount = savingBalance,
                income = periodBalance.income
            )
        }

        val ratioFixedCost = (fixedCostBalance.spend / periodBalance.income) * 100
        val ratioVariableCost = (variableCostBalance.spend / periodBalance.income) * 100
        val ratioSaving = (savingBalance / periodBalance.income) * 100

        return GetBudgetingRuleAnalyticOutput(
            ratioSaving = ratioSaving,
            ratioFixCost = ratioFixedCost,
            ratioVariableCost = ratioVariableCost,
            fixCost = fixedCostBalance.spend,
            variableCost = variableCostBalance.spend,
            savingAmount = savingBalance,
            income = periodBalance.income
        )
    }

    private fun getSavingBalance(startDate: LocalDateTime, endDate: LocalDateTime): Double {
        val accounts = accountRepo.getAll(QueryFilter(0, 0, true))
        val savingAccountType = setOf(AccountType.SAVING, AccountType.BROKING)
        val savingAccountIds = accounts.items.filter{ savingAccountType.contains(it.detail.getType()) }.map { it.id }

        val savingGoalBalance = getBalance.execAsync(GetBalanceInput(
            startDate = startDate,
            endDate = endDate,
            categoryIds = setOf(SAVING_CATEGORY_ID)
        ))

        val savingAccountBalance = getBalance.execAsync(GetBalanceInput(
            accountIds = savingAccountIds.toSet(),
            startDate = startDate,
            endDate = endDate,
            removeSystemCategory = false
        ))

        val total = savingGoalBalance.spend + savingAccountBalance.balance

        return if (total < 0) 0.0 else total
    }
}