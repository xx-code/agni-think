package dev.auguste.mcp.controllers.models

import domain.enums.PeriodType
import org.springframework.ai.mcp.annotation.McpToolParam
import usecases.budgets.dto.GetAllBudgetInput

data class BudgetSearchModel(
    @field:McpToolParam(
        description = "Pagination and sorting options. Omit this field to get the first 15 budgets.",
        required = false,
    )
    val page: PageQueryModel = PageQueryModel(),

    @field:McpToolParam(
        description = "Optional recurrence filter. Accepted values: \"Year\", \"Month\", \"Week\", \"Day\" (case-insensitive). Omit or send an empty list to include every period type.",
        required = false,
    )
    val periodTypes: List<String>? = null,

    @field:McpToolParam(
        description = "When true, each budget includes its live current balance. Set false for a faster response when balances are not needed. Default: true.",
        required = false,
    )
    val loadBalance: Boolean = true,
) {
    fun toDomain(): GetAllBudgetInput {
        return GetAllBudgetInput(
            query = page.toDomain(),
            periodTypes = periodTypes?.map { PeriodType.fromString(it) }?.toSet(),
            loadBalance = loadBalance,
        )
    }
}
