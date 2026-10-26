package dev.auguste.mcp.controllers

import dev.auguste.mcp.controllers.models.BudgetingRuleAnalyticModel
import dev.auguste.mcp.controllers.models.ForecastSpendingModel
import dev.auguste.mcp.controllers.models.SavingAnalyticModel
import dev.auguste.mcp.controllers.models.SpendByCategoryAnalyticModel
import kotlinx.coroutines.runBlocking
import org.springframework.ai.mcp.annotation.McpTool
import org.springframework.ai.mcp.annotation.McpToolParam
import org.springframework.stereotype.Component
import usecases.analystics.dto.ForcastSpendingInput
import usecases.analystics.dto.ForcastSpendingOutput
import usecases.analystics.dto.GetAnnualOutlookOutput
import usecases.analystics.dto.GetBudgetingRuleAnalyticInput
import usecases.analystics.dto.GetBudgetingRuleAnalyticOutput
import usecases.analystics.dto.GetFinanceProfileOutput
import usecases.analystics.dto.GetSavingAnalyticInput
import usecases.analystics.dto.GetSavingAnalyticOutput
import usecases.analystics.dto.GetScheduleInvoiceSummaryOutput
import usecases.analystics.dto.GetSpendByCategoryInput
import usecases.analystics.dto.GetSpendByCategoryOutput
import usecases.dto.ListOutput
import usecases.interfaces.IUseCase

@Component
class AnalyticController(
    private val getSpendCategoryAnalytic: IUseCase<GetSpendByCategoryInput, ListOutput<GetSpendByCategoryOutput>>,
    private val getFinanceProfile: IUseCase<Unit, GetFinanceProfileOutput>,
    private val getSavingAnalytic: IUseCase<GetSavingAnalyticInput, GetSavingAnalyticOutput>,
    private val getBudgetingRuleAnalytic: IUseCase<GetBudgetingRuleAnalyticInput, GetBudgetingRuleAnalyticOutput>,
    private val getAnnualOutlook: IUseCase<Unit, GetAnnualOutlookOutput>,
    private val getScheduleInvoiceSummary: IUseCase<Unit, GetScheduleInvoiceSummaryOutput>,
    private val forcastSpending: IUseCase<ForcastSpendingInput, ForcastSpendingOutput>,
) {

    @McpTool(
        name = "getFinanceProfile",
        description = "Return a compact financial profile: total balance including frozen money, credit utilization, every account, financial principles to follow, upcoming spending, upcoming revenue and income sources. Use this first for a broad question about the user's financial situation.",
        annotations = McpTool.McpAnnotations(
            title = "Get finance profile",
            readOnlyHint = true,
            destructiveHint = false,
            idempotentHint = true,
            openWorldHint = false,
        ),
    )
    fun getFinanceProfile(): GetFinanceProfileOutput {
        return runBlocking { getFinanceProfile.execute(Unit).getOrThrow() }
    }

    @McpTool(
        name = "getSpendByCategory",
        description = "Return spending grouped by category over time. Each item contains the category id, title, icon, color and the list of spending amounts per period. Use this to answer questions such as \"where does my money go?\" or to find the biggest spending categories.",
        annotations = McpTool.McpAnnotations(
            title = "Get spending by category",
            readOnlyHint = true,
            destructiveHint = false,
            idempotentHint = true,
            openWorldHint = false,
        ),
    )
    fun getSpendByCategory(
        @McpToolParam(description = "Analysis options: time period, number of periods, start date and pagination.")
        input: SpendByCategoryAnalyticModel,
    ): ListOutput<GetSpendByCategoryOutput> {
        return runBlocking { getSpendCategoryAnalytic.execute(input.toDomain()).getOrThrow() }
    }

    @McpTool(
        name = "getSavingAnalytic",
        description = "Return savings analytics over time: saved amounts, invested amounts, saving rate and investment rate per period. Use this to evaluate saving habits or compare savings across months.",
        annotations = McpTool.McpAnnotations(
            title = "Get saving analytics",
            readOnlyHint = true,
            destructiveHint = false,
            idempotentHint = true,
            openWorldHint = false,
        ),
    )
    fun getSavingAnalytic(
        @McpToolParam(description = "Analysis options: time period, number of periods and start date.")
        input: SavingAnalyticModel,
    ): GetSavingAnalyticOutput {
        return runBlocking { getSavingAnalytic.execute(input.toDomain()).getOrThrow() }
    }

    @McpTool(
        name = "getBudgetingRuleAnalytic",
        description = "Return how income is split between savings, fixed costs and variable costs, with amounts and ratios. Use this to check whether the user follows a budgeting rule such as 50/30/20.",
        annotations = McpTool.McpAnnotations(
            title = "Get budgeting rule analytics",
            readOnlyHint = true,
            destructiveHint = false,
            idempotentHint = true,
            openWorldHint = false,
        ),
    )
    fun getBudgetingRuleAnalytic(
        @McpToolParam(description = "Analysis options: optional period, interval, start date and end date.")
        input: BudgetingRuleAnalyticModel,
    ): GetBudgetingRuleAnalyticOutput {
        return runBlocking { getBudgetingRuleAnalytic.execute(input.toDomain()).getOrThrow() }
    }

    @McpTool(
        name = "getAnnualOutlook",
        description = "Return the annual outlook: expected income, expected spending, budget outlook, saving margin and expected spending by category, both for the whole year and for the current point in time. Use this for long-term or end-of-year projections.",
        annotations = McpTool.McpAnnotations(
            title = "Get annual outlook",
            readOnlyHint = true,
            destructiveHint = false,
            idempotentHint = true,
            openWorldHint = false,
        ),
    )
    fun getAnnualOutlook(): GetAnnualOutlookOutput {
        return runBlocking { getAnnualOutlook.execute(Unit).getOrThrow() }
    }

    @McpTool(
        name = "getScheduleInvoiceSummary",
        description = "Return the summary of scheduled invoices: total plans, active plans, paused plans and total active amount. Use this to know how many recurring bills or incomes are planned.",
        annotations = McpTool.McpAnnotations(
            title = "Get scheduled invoice summary",
            readOnlyHint = true,
            destructiveHint = false,
            idempotentHint = true,
            openWorldHint = false,
        ),
    )
    fun getScheduleInvoiceSummary(): GetScheduleInvoiceSummaryOutput {
        return runBlocking { getScheduleInvoiceSummary.execute(Unit).getOrThrow() }
    }

    @McpTool(
        name = "forecastSpending",
        description = "Forecast the financial situation between two dates. It combines account balances, scheduled invoices, budgets and the target saving rate, then reports the remaining amount and whether optional purchases (wantItems) can be approved. Use this for questions such as \"can I afford this purchase?\" or \"how much will I have left next month?\".",
        annotations = McpTool.McpAnnotations(
            title = "Forecast spending",
            readOnlyHint = true,
            destructiveHint = false,
            idempotentHint = true,
            openWorldHint = false,
        ),
    )
    fun forecastSpending(
        @McpToolParam(description = "Forecast options: date range, optional purchases, extra saving income, limits and saving rate.")
        input: ForecastSpendingModel,
    ): ForcastSpendingOutput {
        return runBlocking { forcastSpending.execute(input.toDomain()).getOrThrow() }
    }
}
