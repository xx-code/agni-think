package dev.auguste.mcp.controllers

import kotlinx.coroutines.runBlocking
import org.springframework.ai.mcp.annotation.McpTool
import org.springframework.stereotype.Component
import usecases.analystics.dto.GetFinanceProfileOutput
import usecases.interfaces.IUseCase

@Component
class AnalyticController(
    private val getFinanceProfile: IUseCase<Unit, GetFinanceProfileOutput>,
) {
    @McpTool(
        name = "getFinanceProfile",
        description = "get a short resume of the finance profile",
    )
    fun getFinanceProfile() : GetFinanceProfileOutput {
        val result = runBlocking { getFinanceProfile.execute(Unit).getOrThrow() }
        return result
    }
}