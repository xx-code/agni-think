package dev.auguste.agni_api.core.usecases

data class ListOutput<TDto>(
    val items: List<TDto>,
    val total: Long
)
