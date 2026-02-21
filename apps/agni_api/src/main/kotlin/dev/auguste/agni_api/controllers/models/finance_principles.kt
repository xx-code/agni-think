package dev.auguste.agni_api.controllers.models

import dev.auguste.agni_api.core.entities.enums.PrincipleType
import dev.auguste.agni_api.core.usecases.finance_principles.dto.CreateFinancePrincipleInput
import dev.auguste.agni_api.core.usecases.finance_principles.dto.UpdateFinancePrincipleInput
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import java.time.LocalDate
import java.util.UUID

data class ApiCreateFinancePrincipleModel(
    @field:NotEmpty(message = "Title must not be empty")
    val name: String,
    @field:NotEmpty(message = "Description must not be empty")
    val description: String,
    @field:NotNull(message = "Target Type must not be empty")
    val targetType: String,
    @field:Min(0, message = "Strictness value must be positive")
    val strictness: Int,

    val logicRules: String?
)

data class ApiUpdateFinancePrincipleModel(
    val name: String?,
    val description: String?,
    val targetType: String?,
    val strictness: Int?,
    val logicRules: String?,
)

fun mapApiCreateFinancePrincipleTo(input: ApiCreateFinancePrincipleModel): CreateFinancePrincipleInput {
    return CreateFinancePrincipleInput(
        input.name,
        input.description,
        PrincipleType.fromString(input.targetType),
        input.strictness,
        input.logicRules
    )
}

fun mapApiUpdateFinancePrincipleTo(id: UUID, input: ApiUpdateFinancePrincipleModel): UpdateFinancePrincipleInput {
    return UpdateFinancePrincipleInput(
        id = id,
        name = input.name,
        description = input.description,
        strictness = input.strictness,
        logicRules = input.logicRules,
        targetType = input.targetType?.let { PrincipleType.fromString(it) }
    )
}