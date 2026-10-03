package usecases.analystics

import usecases.UseCase
import domain.utils.LocalDateTimeRange
import usecases.analystics.dto.GetBudgetingRuleAnalyticInput
import usecases.analystics.dto.GetBudgetingRuleAnalyticOutput
import usecases.analystics.dto.GetSavingBalanceInput
import usecases.interfaces.IUseCase
import usecases.invoices.dto.GetBalanceInput
import usecases.invoices.dto.GetBalanceOutput
import domain.enums.InvoiceType

class GetBudgetingRuleAnalytic(
    private val getBalance: IUseCase<GetBalanceInput, GetBalanceOutput>,
    private val getSavingBalance: IUseCase<GetSavingBalanceInput, Double>,
): UseCase<GetBudgetingRuleAnalyticInput, GetBudgetingRuleAnalyticOutput>() {
    override suspend fun process(input: GetBudgetingRuleAnalyticInput): GetBudgetingRuleAnalyticOutput {
        val range = if (input.startDate != null && input.endDate != null) {
            LocalDateTimeRange(input.startDate, input.endDate)
        } else {
            LocalDateTimeRange.fromPeriod(input.period!!, input.interval)
        }

        val periodBalance = getBalance.processDirect(GetBalanceInput(
            startDate = range.start,
            endDate = range.end,
        ))

        val fixedCostBalance = getBalance.processDirect(GetBalanceInput(
            types = setOf(InvoiceType.FIXED_COST),
            startDate = range.start,
            endDate = range.end,
        ))

        val variableCostBalance = getBalance.processDirect(GetBalanceInput(
            types = setOf(InvoiceType.VARIABLE_COST),
            startDate = range.start,
            endDate = range.end
        ))

        val savingBalance = getSavingBalance.processDirect(
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