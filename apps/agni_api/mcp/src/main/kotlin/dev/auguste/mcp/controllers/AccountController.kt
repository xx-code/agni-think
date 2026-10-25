package dev.auguste.mcp.controllers

import dev.auguste.mcp.controllers.models.AccountSearchModel
import kotlinx.coroutines.runBlocking
import org.springframework.ai.mcp.annotation.McpTool
import org.springframework.ai.mcp.annotation.McpToolParam
import org.springframework.stereotype.Component
import usecases.accounts.dto.GetAccountOutput
import usecases.accounts.dto.GetAccountWithDetailOutput
import usecases.accounts.dto.GetAllAccountInput
import usecases.accounts.dto.GetTotalBalanceAmountOutput
import usecases.dto.ListOutput
import usecases.interfaces.IUseCase
import java.util.UUID

@Component
class AccountController(
    private val getAllAccountWithDetailUseCase: IUseCase<GetAllAccountInput, ListOutput<GetAccountWithDetailOutput>>,
    private val getTotalBalanceUseCase: IUseCase<Unit, GetTotalBalanceAmountOutput>,
) {
    @McpTool(
        name = "getAllAccountsWithDetail",
        description = "List bank accounts with full detail for each account: balances, locks, and type-specific rules (credit limit, utilization, buffer). Use this for a complete financial overview of all accounts.",
        annotations = McpTool.McpAnnotations(
            title = "List accounts with detail",
            readOnlyHint = true,
            destructiveHint = false,
            idempotentHint = true,
            openWorldHint = false,
        ),
    )
    fun getAllAccountsWithDetail(
        @McpToolParam(description = "Pagination and sorting options. Omit or send an empty object to get the first 15 accounts.")
        input: AccountSearchModel,
    ): ListOutput<GetAccountWithDetailOutput> {
        return runBlocking { getAllAccountWithDetailUseCase.execute(input.toDomain()).getOrThrow() }
    }

    @McpTool(
        name = "getTotalBalance",
        description = "Return the consolidated balance across all accounts: total balance, available amount, frozen amount, locked amount, credit utilization and buffer information. Use this for questions such as \"how much money do I have?\".",
        annotations = McpTool.McpAnnotations(
            title = "Get total balance",
            readOnlyHint = true,
            destructiveHint = false,
            idempotentHint = true,
            openWorldHint = false,
        ),
    )
    fun getTotalBalance(): GetTotalBalanceAmountOutput {
        return runBlocking { getTotalBalanceUseCase.execute(Unit).getOrThrow() }
    }
}
