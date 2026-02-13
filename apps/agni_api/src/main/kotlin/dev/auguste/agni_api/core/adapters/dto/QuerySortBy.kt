package dev.auguste.agni_api.core.adapters.dto

data class QuerySortBy(
    var by: String,
    var ascending: Boolean = false
)
