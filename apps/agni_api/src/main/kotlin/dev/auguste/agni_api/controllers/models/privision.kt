package dev.auguste.agni_api.controllers.models

import dev.auguste.agni_api.core.usecases.provisionable.dto.CreateProvisionInput
import dev.auguste.agni_api.core.usecases.provisionable.dto.UpdateProvisionInput
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import java.time.LocalDate
import java.util.UUID

data class ApiCreateProvisionModel(
    @field:NotEmpty(message = "Title must not be empty")
    val title: String,
    @field:Min(0, message = "initial value must be positive")
    val initialCost: Double,
    @field:NotNull(message = "Acquisition date must be set")
    val acquisitionDate: LocalDate,
    @field:Min(0, message = "Expected Life span month")
    val expectedLifespanMonth: Int,
    @field:NotNull(message = "Residual value must be set")
    val residualValue: Double
)

data class ApiUpdateProvisionModel(
    val title: String?,
    val initialCost: Double?,
    val acquisitionDate: LocalDate?,
    val expectedLifespanMonth: Int?,
    val residualValue: Double?
)

fun mapApiCreateProvision(model: ApiCreateProvisionModel): CreateProvisionInput {
    return CreateProvisionInput(
        title = model.title,
        initialCost = model.initialCost,
        acquisitionDate = model.acquisitionDate,
        expectedLifespanMonth = model.expectedLifespanMonth,
        residualValue = model.residualValue
    )
}

fun mapApiUpdateProvision(id: UUID, model:ApiUpdateProvisionModel): UpdateProvisionInput {
    return UpdateProvisionInput(
        id = id,
        title = model.title,
        initialCost = model.initialCost,
        acquisitionDate = model.acquisitionDate,
        expectedLifespanMonth = model.expectedLifespanMonth,
        residualValue = model.residualValue
    )
}