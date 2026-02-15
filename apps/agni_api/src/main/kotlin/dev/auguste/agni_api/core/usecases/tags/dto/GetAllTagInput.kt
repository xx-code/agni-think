package dev.auguste.agni_api.core.usecases.tags.dto

import dev.auguste.agni_api.core.adapters.dto.QueryFilter

data class GetAllTagInput(
    val query: QueryFilter,
    val isSystem: Boolean? = null
)
