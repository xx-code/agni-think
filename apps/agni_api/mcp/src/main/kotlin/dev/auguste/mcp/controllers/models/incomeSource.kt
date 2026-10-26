package dev.auguste.mcp.controllers.models

import org.springframework.ai.mcp.annotation.McpToolParam
import usecases.income_sources.dto.GetAllIncomeSourceInput

data class IncomeSourceSearchModel(
    @field:McpToolParam(
        description = "Pagination and sorting options. Omit this field to get the first 15 income sources.",
        required = false,
    )
    val page: PageQueryModel = PageQueryModel(),
) {
    fun toDomain(): GetAllIncomeSourceInput {
        return GetAllIncomeSourceInput(queryFilter = page.toDomain())
    }
}
