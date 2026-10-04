package usecases.dto

data class ListOutput<TDto>(
    val items: List<TDto>,
    val total: Long
)
