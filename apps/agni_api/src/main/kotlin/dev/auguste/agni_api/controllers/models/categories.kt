package dev.auguste.agni_api.controllers.models

import dev.auguste.agni_api.core.usecases.categories.dto.CreateCategoryInput
import dev.auguste.agni_api.core.usecases.categories.dto.UpdateCategoryInput
import java.util.UUID

data class ApiCreateCategoryModel(
    val title: String,
    val icon: String,
    val color: String
)

data class ApiUpdateCategoryModel(
    val title: String?,
    val icon: String?,
    val color: String?
)

fun mapApiCreateCategoryModel(model: ApiCreateCategoryModel): CreateCategoryInput {
   return CreateCategoryInput(
       title = model.title,
       icon = model.icon,
       color = model.color,
       isSystem = false
   )
}

fun mapApiUpdateCategoryModel(id: UUID, model: ApiUpdateCategoryModel): UpdateCategoryInput {
    return UpdateCategoryInput(
        id = id,
        title = model.title,
        icon = model.icon,
        color = model.color
    )
}
