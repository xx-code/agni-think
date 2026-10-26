package dev.auguste.mcp.controllers.models

import org.springframework.ai.mcp.annotation.McpToolParam
import usecases.accounts.dto.GetAllAccountInput

data class AccountSearchModel(
    @field:McpToolParam(
        description = "Pagination and sorting options. Omit this field to get the first 15 accounts.",
        required = false,
    )
    val page: PageQueryModel = PageQueryModel(),
) {
    fun toDomain(): GetAllAccountInput {
        return GetAllAccountInput(queryFilter = page.toDomain())
    }
}
