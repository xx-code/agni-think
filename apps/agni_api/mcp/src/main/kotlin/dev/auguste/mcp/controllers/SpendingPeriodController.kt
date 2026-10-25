package dev.auguste.mcp.controllers

import dev.auguste.mcp.controllers.models.SpendingPeriodSearchModel
import kotlinx.coroutines.runBlocking
import org.springframework.ai.mcp.annotation.McpTool
import org.springframework.ai.mcp.annotation.McpToolParam
import org.springframework.stereotype.Component
import usecases.dto.ListOutput
import usecases.interfaces.IUseCase
import usecases.spending_period.dto.GetAllSpendingPeriodInput
import usecases.spending_period.dto.GetAllSpendingPeriodOutput

@Component
class SpendingPeriodController(
    private val getAllSpendingPeriodUseCase: IUseCase<GetAllSpendingPeriodInput, ListOutput<GetAllSpendingPeriodOutput>>,
) {

    @McpTool(
        name = "getAllSpendingPeriods",
        description = "List spending periods with their template, date range, saving rate target, free amount, state, snapshot and wishlist items. Supports filtering by template and state. Sort by \"startDate\" descending to find the most recent period.",
        annotations = McpTool.McpAnnotations(
            title = "List spending periods",
            readOnlyHint = true,
            destructiveHint = false,
            idempotentHint = true,
            openWorldHint = false,
        ),
    )
    fun getAllSpendingPeriods(
        @McpToolParam(description = "Search options: pagination, optional template filter and optional state filter.")
        input: SpendingPeriodSearchModel,
    ): ListOutput<GetAllSpendingPeriodOutput> {
        return runBlocking { getAllSpendingPeriodUseCase.execute(input.toDomain()).getOrThrow() }
    }
}
