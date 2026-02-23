package dev.auguste.agni_api.core.usecases.analystics

import dev.auguste.agni_api.core.entities.enums.InvoiceType
import dev.auguste.agni_api.core.entities.utils.LocalDateTimeRange
import dev.auguste.agni_api.core.usecases.analystics.dto.GetBudgetingRuleAnalyticInput
import dev.auguste.agni_api.core.usecases.analystics.dto.GetBudgetingRuleAnalyticOutput
import dev.auguste.agni_api.core.usecases.analystics.dto.GetSavingBalanceInput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.invoices.dto.GetBalanceInput
import dev.auguste.agni_api.core.usecases.invoices.dto.GetBalanceOutput

class GetBudgetingRuleAnalytic(
    private val getBalance: IUseCase<GetBalanceInput, GetBalanceOutput>,
    private val getSavingBalance: IUseCase<GetSavingBalanceInput, Double>,
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

        val savingBalance = getSavingBalance.execAsync(
            GetSavingBalanceInput(range.start, range.end)
        )

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

        val ratioFixedCost = (fixedCostBalance.spend / periodBalance.income)
        val ratioVariableCost = (variableCostBalance.spend / periodBalance.income)
        val ratioSaving = (savingBalance / periodBalance.income)

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
}