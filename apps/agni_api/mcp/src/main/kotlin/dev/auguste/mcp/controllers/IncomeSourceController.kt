package dev.auguste.mcp.controllers

import dev.auguste.mcp.controllers.models.IncomeSourceSearchModel
import kotlinx.coroutines.runBlocking
import org.springframework.ai.mcp.annotation.McpTool
import org.springframework.ai.mcp.annotation.McpToolParam
import org.springframework.stereotype.Component
import usecases.dto.ListOutput
import usecases.income_sources.dto.GetAllIncomeSourceInput
import usecases.income_sources.dto.GetIncomeSourceOutput
import usecases.interfaces.IUseCase
import java.util.UUID

@Component
class IncomeSourceController(
    private val getAllIncomeSourceUseCase: IUseCase<GetAllIncomeSourceInput, ListOutput<GetIncomeSourceOutput>>,
) {
    @McpTool(
        name = "getAllIncomeSources",
        description = "List all income sources with their type, frequency, reliability, tax rate, next estimated net amount and end date. Use this to understand where the user's money comes from.",
        annotations = McpTool.McpAnnotations(
            title = "List income sources",
            readOnlyHint = true,
            destructiveHint = false,
            idempotentHint = true,
            openWorldHint = false,
        ),
    )
    fun getAllIncomeSources(
        @McpToolParam(description = "Pagination and sorting options. Omit or send an empty object to get the first 15 income sources.")
        input: IncomeSourceSearchModel,
    ): ListOutput<GetIncomeSourceOutput> {
        return runBlocking { getAllIncomeSourceUseCase.execute(input.toDomain()).getOrThrow() }
    }
}
