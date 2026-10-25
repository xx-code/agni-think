package dev.auguste.mcp.controllers

import dev.auguste.mcp.controllers.models.BudgetSearchModel
import kotlinx.coroutines.runBlocking
import org.springframework.ai.mcp.annotation.McpTool
import org.springframework.ai.mcp.annotation.McpToolParam
import org.springframework.stereotype.Component
import usecases.budgets.dto.GetAllBudgetInput
import usecases.budgets.dto.GetBudgetOutput
import usecases.dto.ListOutput
import usecases.interfaces.IUseCase
import java.util.UUID

@Component
class BudgetController(
    private val getAllBudgets: IUseCase<GetAllBudgetInput, ListOutput<GetBudgetOutput>>
) {
    @McpTool(
        name = "getAllBudgets",
        description = "List budgets with their target, current balance, due date and recurrence. Supports filtering by period type and optional loading of live balances. Use this to review all spending limits or find a budget UUID.",
        annotations = McpTool.McpAnnotations(
            title = "List budgets",
            readOnlyHint = true,
            destructiveHint = false,
            idempotentHint = true,
            openWorldHint = false,
        ),
    )
    fun getAllBudgets(
        @McpToolParam(description = "Search options: pagination, optional period type filter and whether to load balances.")
        input: BudgetSearchModel,
    ): ListOutput<GetBudgetOutput> {
        return runBlocking { getAllBudgets.execute(input.toDomain()).getOrThrow() }
    }
}
