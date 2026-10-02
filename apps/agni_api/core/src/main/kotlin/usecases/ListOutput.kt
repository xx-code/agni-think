package usecases

data class ListOutput<TDto>(
    val items: List<TDto>,
    val total: Long
)
