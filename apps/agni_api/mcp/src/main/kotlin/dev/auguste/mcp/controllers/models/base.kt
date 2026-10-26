package dev.auguste.mcp.controllers.models

import adapters.dto.QueryFilter
import adapters.dto.QuerySortBy
import org.springframework.ai.mcp.annotation.McpToolParam

data class PageQueryModel(
    @field:McpToolParam(
        description = "Zero-based index of the first item to return. Use 0 for the first page. Default: 0.",
        required = false,
    )
    val offset: Int = 0,

    @field:McpToolParam(
        description = "Maximum number of items to return in one page. Prefer 10 to 50 to keep answers concise. Default: 15.",
        required = false,
    )
    val limit: Int = 15,

    @field:McpToolParam(
        description = "When true, ignore offset/limit and return every matching item. Use true only for small collections. Default: false.",
        required = false,
    )
    val queryAll: Boolean = false,

    @field:McpToolParam(
        description = "Name of the field used to sort the results, for example \"title\" or \"dueDate\". Leave empty to keep the default order.",
        required = false,
    )
    val sortBy: String = "",

    @field:McpToolParam(
        description = "When true, results are sorted ascending. When false, results are sorted descending. Default: false.",
        required = false,
    )
    val ascending: Boolean = false,
) {
    fun toDomain(): QueryFilter {
        return QueryFilter(
            offset = offset,
            limit = limit,
            queryAll = queryAll,
            sortBy = QuerySortBy(sortBy, ascending),
        )
    }
}
