package usecases.tags.dto

data class CreateTagInput(
    val value: String,
    val color: String,
    val isSystem: Boolean? = false
)
