package dev.auguste.mcp.controllers.models

import org.springframework.ai.mcp.annotation.McpToolParam
import usecases.tags.dto.GetAllTagInput

data class TagSearchModel(
    @field:McpToolParam(
        description = "Pagination and sorting options. Omit this field to get the first 15 tags.",
        required = false,
    )
    val page: PageQueryModel = PageQueryModel(),

    @field:McpToolParam(
        description = "Filter by origin. true returns only built-in system tags, false returns only tags created by the user, null or omitted returns both.",
        required = false,
    )
    val isSystem: Boolean? = null,

    @field:McpToolParam(
        description = "Filter by archive state. true returns only archived tags, false returns only active tags, null or omitted returns both.",
        required = false,
    )
    val isArchived: Boolean? = null,
) {
    fun toDomain(): GetAllTagInput {
        return GetAllTagInput(
            query = page.toDomain(),
            isSystem = isSystem,
            isArchived = isArchived,
        )
    }
}
