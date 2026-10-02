package dev.auguste.agni_api.controllers.models

import domain.enums.DeductionBaseType
import domain.enums.DeductionModeType
import usecases.deductions.dto.CreateDeductionInput
import usecases.deductions.dto.UpdateDeductionInput
import jakarta.validation.constraints.NotEmpty
import java.util.UUID

data class ApiCreateDeductionModel(
    @field:NotEmpty("Title must not be empty")
    val title: String,
    val description: String,
    @field:NotEmpty("Base must not be empty")
    val base: String,
    @field:NotEmpty("Mode must not be empty")
    val mode: String,
)

data class ApiUpdateDeductionModel(
    val title: String?,
    val description: String?
)

fun mapApiCreateDeduction(model: ApiCreateDeductionModel): CreateDeductionInput {
   return CreateDeductionInput(
       title = model.title,
       description = model.description,
       base = DeductionBaseType.fromString(model.base),
       mode = DeductionModeType.fromString(model.mode)
   )
}

fun mapApiUpdateDeduction(id: UUID, model: ApiUpdateDeductionModel): UpdateDeductionInput {
   return UpdateDeductionInput(
       id = id,
       title = model.title,
       description = model.description,
   )
}
