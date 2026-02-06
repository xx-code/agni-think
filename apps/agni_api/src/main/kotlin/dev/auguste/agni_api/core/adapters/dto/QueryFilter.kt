package dev.auguste.agni_api.core.adapters.dto

data class QueryFilter(
    val offset: Int,
    val limit: Int,
    val queryAll: Boolean = false,
    val sortBy: QuerySortBy = QuerySortBy("", false)
)
