package usecases.categories.dto

import adapters.dto.QueryFilter

data class GetAllCategoryInput(
    val query: QueryFilter,
    val isSystem: Boolean? = null,
    val isArchived: Boolean? = null
)
