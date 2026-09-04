package dev.auguste.agni_api.core.usecases.categories.dto

import java.util.UUID

data class UpdateCategoryInput(
    val id: UUID,
    val title: String? = null,
    val icon: String? = null,
    val color: String? = null,
    val isArchived: Boolean? = null
)
