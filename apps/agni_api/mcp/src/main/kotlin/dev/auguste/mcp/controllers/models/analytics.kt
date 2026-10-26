package dev.auguste.mcp.controllers.models

import domain.enums.PeriodType
import org.springframework.ai.mcp.annotation.McpToolParam
import usecases.analystics.dto.ForcastSpendingInput
import usecases.analystics.dto.GetBudgetingRuleAnalyticInput
import usecases.analystics.dto.GetSavingAnalyticInput
import usecases.analystics.dto.GetSpendByCategoryInput
import usecases.analystics.dto.SavingAdditionalIncomeInput
import usecases.analystics.dto.WantItemOutput
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class SpendByCategoryAnalyticModel(
    @field:McpToolParam(
        description = "Time bucket used to group spending. Accepted values: \"Day\", \"Week\", \"Month\", \"Year\" (case-insensitive). Example: \"Month\" for monthly totals.",
    )
    val period: String,

    @field:McpToolParam(
        description = "Number of periods to include. Use 1 for the current period, 3 for the last three periods, etc. Default: 1.",
        required = false,
    )
    val interval: Int = 1,

    @field:McpToolParam(
        description = "Start of the analysis window in ISO-8601 date-time format, for example \"2026-01-01T00:00:00\". Defaults to now when omitted.",
        required = false,
    )
    val startDate: LocalDateTime? = null,

    @field:McpToolParam(
        description = "Pagination and sorting for the returned category list. Omit to get the first 15 categories.",
        required = false,
    )
    val page: PageQueryModel = PageQueryModel(),
) {
    fun toDomain(): GetSpendByCategoryInput {
        return GetSpendByCategoryInput(
            period = PeriodType.fromString(period),
            interval = interval,
            startDate = startDate ?: LocalDateTime.now(),
            query = page.toDomain(),
        )
    }
}

data class SavingAnalyticModel(
    @field:McpToolParam(
        description = "Time bucket used to group savings. Accepted values: \"Day\", \"Week\", \"Month\", \"Year\" (case-insensitive). Example: \"Month\".",
    )
    val period: String,

    @field:McpToolParam(
        description = "Number of periods to include. Use 1 for the current period, 12 for the last twelve periods, etc. Default: 1.",
        required = false,
    )
    val interval: Int = 1,

    @field:McpToolParam(
        description = "Start of the analysis window in ISO-8601 date-time format, for example \"2026-01-01T00:00:00\". Defaults to now when omitted.",
        required = false,
    )
    val startDate: LocalDateTime? = null,
) {
    fun toDomain(): GetSavingAnalyticInput {
        return GetSavingAnalyticInput(
            period = PeriodType.fromString(period),
            interval = interval,
            startDate = startDate ?: LocalDateTime.now(),
        )
    }
}

data class BudgetingRuleAnalyticModel(
    @field:McpToolParam(
        description = "Optional time bucket. Accepted values: \"Day\", \"Week\", \"Month\", \"Year\". Default when omitted: \"Month\".",
        required = false,
    )
    val period: String? = null,

    @field:McpToolParam(
        description = "Number of periods to include. Use 0 or omit for the default calculation. Default: 0.",
        required = false,
    )
    val interval: Int = 0,

    @field:McpToolParam(
        description = "Optional start of the analysis window in ISO-8601 date-time format, for example \"2026-01-01T00:00:00\".",
        required = false,
    )
    val startDate: LocalDateTime? = null,

    @field:McpToolParam(
        description = "Optional end of the analysis window in ISO-8601 date-time format, for example \"2026-12-31T23:59:59\".",
        required = false,
    )
    val endDate: LocalDateTime? = null,
) {
    fun toDomain(): GetBudgetingRuleAnalyticInput {
        return GetBudgetingRuleAnalyticInput(
            startDate = startDate,
            endDate = endDate,
            period = period?.let { PeriodType.fromString(it) } ?: PeriodType.MONTH,
            interval = interval,
        )
    }
}

data class ForecastWantItemModel(
    @field:McpToolParam(
        description = "Human-readable name of the optional purchase or expense to evaluate, for example \"New laptop\".",
    )
    val description: String,

    @field:McpToolParam(
        description = "Cost of the item in the account currency. Must be greater than or equal to 0.",
    )
    val amount: Double,
) {
    fun toDomain(): WantItemOutput {
        return WantItemOutput(description = description, amount = amount)
    }
}

data class ForecastSavingAdditionalIncomeModel(
    @field:McpToolParam(
        description = "UUID of the saving account that receives the additional income.",
    )
    val savingAccountId: UUID,

    @field:McpToolParam(
        description = "Additional amount credited to that saving account during the forecast window.",
    )
    val amount: Double,
) {
    fun toDomain(): SavingAdditionalIncomeInput {
        return SavingAdditionalIncomeInput(savingAccountId = savingAccountId, amount = amount)
    }
}

data class ForecastSpendingModel(
    @field:McpToolParam(
        description = "First day of the forecast window in ISO-8601 date format, for example \"2026-11-01\".",
    )
    val startDate: LocalDate,

    @field:McpToolParam(
        description = "Last day of the forecast window in ISO-8601 date format, for example \"2026-11-30\".",
    )
    val endDate: LocalDate,

    @field:McpToolParam(
        description = "Optional purchases to test against the remaining budget. Send an empty list to only forecast income and expenses.",
        required = false,
    )
    val wantItems: List<ForecastWantItemModel> = emptyList(),

    @field:McpToolParam(
        description = "Optional extra income to add to saving accounts during the forecast. Send an empty list when not applicable.",
        required = false,
    )
    val savingAdditionalIncome: List<ForecastSavingAdditionalIncomeModel> = emptyList(),

    @field:McpToolParam(
        description = "Optional UUIDs of budgets to include in the forecast. Send an empty list to use the default budgets.",
        required = false,
    )
    val budgetIds: List<UUID> = emptyList(),

    @field:McpToolParam(
        description = "Optional replacement for the sum of all account balances. Omit to use the real current balances.",
        required = false,
    )
    val overrideAccountsBalance: Double? = null,

    @field:McpToolParam(
        description = "Optional saving rate percentage between 0 and 100. Omit to use the user's profile saving rate.",
        required = false,
    )
    val savingRate: Double? = null,
) {
    fun toDomain(): ForcastSpendingInput {
        return ForcastSpendingInput(
            startDate = startDate,
            endDate = endDate,
            wantItems = wantItems.map { it.toDomain() },
            savingAdditionalIncome = savingAdditionalIncome.map { it.toDomain() },
            budgetIds = budgetIds,
            overrideAccountsBalance = overrideAccountsBalance,
            savingRate = savingRate,
        )
    }
}
