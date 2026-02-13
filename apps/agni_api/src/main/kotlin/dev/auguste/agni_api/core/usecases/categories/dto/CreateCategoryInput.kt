package dev.auguste.agni_api.core.usecases.categories.dto

data class CreateCategoryInput(
    val title: String,
    val icon: String,
    val color: String,
    val isSystem: Boolean?
)
