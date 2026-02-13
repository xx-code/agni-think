package dev.auguste.agni_api.core.adapters.dto

data class QueryFilter(
    val offset: Int = 0,
    val limit: Int = 15,
    val queryAll: Boolean = false,
    val sortBy: QuerySortBy = QuerySortBy("", false)
)
