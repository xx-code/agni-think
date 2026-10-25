package dev.auguste.mcp.controllers

import dev.auguste.mcp.controllers.models.FundSearchModel
import kotlinx.coroutines.runBlocking
import org.springframework.ai.mcp.annotation.McpTool
import org.springframework.ai.mcp.annotation.McpToolParam
import org.springframework.stereotype.Component
import usecases.dto.ListOutput
import usecases.funds.dto.GetAllFundInput
import usecases.funds.dto.GetFundOutput
import usecases.interfaces.IUseCase
import java.util.UUID

@Component
class FundController(
    private val getAllFundUseCase: IUseCase<GetAllFundInput, ListOutput<GetFundOutput>>,
) {

    @McpTool(
        name = "getAllFunds",
        description = "List saving funds with their target, current balance, type, linked account and goals. Supports filtering by fund type. Use this to review all saving goals or find a fund UUID.",
        annotations = McpTool.McpAnnotations(
            title = "List saving funds",
            readOnlyHint = true,
            destructiveHint = false,
            idempotentHint = true,
            openWorldHint = false,
        ),
    )
    fun getAllFunds(
        @McpToolParam(description = "Search options: pagination and optional fund type filter.")
        input: FundSearchModel,
    ): ListOutput<GetFundOutput> {
        return runBlocking { getAllFundUseCase.execute(input.toDomain()).getOrThrow() }
    }
}
