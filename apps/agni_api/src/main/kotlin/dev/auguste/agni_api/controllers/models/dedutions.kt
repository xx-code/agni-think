package dev.auguste.agni_api.controllers.models

import dev.auguste.agni_api.core.entities.enums.DeductionBaseType
import dev.auguste.agni_api.core.entities.enums.DeductionModeType
import dev.auguste.agni_api.core.usecases.deductions.dto.CreateDeductionInput
import dev.auguste.agni_api.core.usecases.deductions.dto.UpdateDeductionInput
import jakarta.validation.constraints.NotEmpty
import java.util.UUID

data class ApiCreateDeductionModel(
    @field:NotEmpty("Title must not be empty")
    val title: String,
    val description: String,
    @field:NotEmpty("Base must not be empty")
    val base: DeductionBaseType,
    @field:NotEmpty("Mode must not be empty")
    val mode: DeductionModeType
)

data class ApiUpdateDeductionModel(
    val title: String?,
    val description: String?
)

fun mapApiCreateDeduction(model: ApiCreateDeductionModel): CreateDeductionInput {
   return CreateDeductionInput(
       title = model.title,
       description = model.description,
       base = model.base,
       mode = model.mode
   )
}

fun mapApiUpdateDeduction(id: UUID, model: ApiUpdateDeductionModel): UpdateDeductionInput {
   return UpdateDeductionInput(
       id = id,
       title = model.title,
       description = model.description,
   )
}
