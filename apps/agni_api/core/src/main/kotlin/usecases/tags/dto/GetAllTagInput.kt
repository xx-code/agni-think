package usecases.tags.dto

import adapters.dto.QueryFilter

data class GetAllTagInput(
    val query: QueryFilter,
    val isSystem: Boolean? = null,
    val isArchived: Boolean? = null,
)
