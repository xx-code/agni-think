package dev.auguste.agni_api.controllers.models

import dev.auguste.agni_api.core.usecases.tags.dto.CreateTagInput
import dev.auguste.agni_api.core.usecases.tags.dto.UpdateTagInput
import jakarta.validation.constraints.NotEmpty
import java.util.UUID

data class ApiCreateTagModel(
    @field:NotEmpty("Value must not be empty")
    val value: String,
    val color: String,
)

data class ApiUpdateTagModel(
    val value: String?,
    val color: String?,
)

fun mapApiCreateTag(model: ApiCreateTagModel): CreateTagInput {
    return CreateTagInput(
        model.value,
        model.color
    )
}

fun mapApiUpdateTag(id: UUID, model: ApiUpdateTagModel): UpdateTagInput {
    return UpdateTagInput(
        id = id,
        value = model.value,
        color = model.color
    )
}