package dev.auguste.agni_api.core.usecases.categories.dto

import dev.auguste.agni_api.core.adapters.dto.QueryFilter

data class GetAllCategoryInput(
    val query: QueryFilter,
    val isSystem: Boolean? = null
)
