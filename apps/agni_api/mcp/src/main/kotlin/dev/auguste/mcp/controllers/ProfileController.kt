package dev.auguste.mcp.controllers

import kotlinx.coroutines.runBlocking
import org.springframework.ai.mcp.annotation.McpTool
import org.springframework.stereotype.Component
import usecases.interfaces.IUseCase
import usecases.profiles.dto.GetProfileOutput

@Component
class ProfileController(
    private val getProfileUseCase: IUseCase<Unit, GetProfileOutput>,
) {

    @McpTool(
        name = "getProfile",
        description = "Return the user's budgeting profile: maximum wishlist amount, target percentages for fixed spending, variable spending and saving, and the required balance buffer. Use this to personalise advice about spending and saving rules.",
        annotations = McpTool.McpAnnotations(
            title = "Get finance profile settings",
            readOnlyHint = true,
            destructiveHint = false,
            idempotentHint = true,
            openWorldHint = false,
        ),
    )
    fun getProfile(): GetProfileOutput {
        return runBlocking { getProfileUseCase.execute(Unit).getOrThrow() }
    }
}
