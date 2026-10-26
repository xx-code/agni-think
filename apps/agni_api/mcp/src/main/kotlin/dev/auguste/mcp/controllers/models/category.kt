package dev.auguste.mcp.controllers.models

import org.springframework.ai.mcp.annotation.McpToolParam
import usecases.categories.dto.GetAllCategoryInput

data class CategorySearchModel(
    @field:McpToolParam(
        description = "Pagination and sorting options. Omit this field to get the first 15 categories.",
        required = false,
    )
    val page: PageQueryModel = PageQueryModel(),

    @field:McpToolParam(
        description = "Filter by origin. true returns only built-in system categories, false returns only categories created by the user, null or omitted returns both.",
        required = false,
    )
    val isSystem: Boolean? = null,

    @field:McpToolParam(
        description = "Filter by archive state. true returns only archived categories, false returns only active categories, null or omitted returns both.",
        required = false,
    )
    val isArchived: Boolean? = null,
) {
    fun toDomain(): GetAllCategoryInput {
        return GetAllCategoryInput(
            query = page.toDomain(),
            isSystem = isSystem,
            isArchived = isArchived,
        )
    }
}
