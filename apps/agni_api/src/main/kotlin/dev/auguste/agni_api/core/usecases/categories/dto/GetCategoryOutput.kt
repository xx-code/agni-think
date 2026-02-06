package dev.auguste.agni_api.core.usecases.categories.dto

import java.util.UUID

data class GetCategoryOutput(
    val id: UUID,
    val title: String,
    val icon: String,
    val color: String,
    val isSystem: Boolean?
)
