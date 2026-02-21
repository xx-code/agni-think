package dev.auguste.agni_api.controllers.models

import dev.auguste.agni_api.core.entities.enums.IncomeSourceFrequencyType
import dev.auguste.agni_api.core.entities.enums.IncomeSourceType
import dev.auguste.agni_api.core.usecases.income_sources.dto.CreateIncomeSourceInput
import dev.auguste.agni_api.core.usecases.income_sources.dto.UpdateIncomeSourceInput
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import java.time.LocalDate
import java.util.UUID

data class ApiCreateIncomeSourceModel(
    @field:NotEmpty("Title must not be empty")
    val title: String,

    @field:NotEmpty("type must not be empty")
    val type: String,

    @field:NotEmpty("type must not be empty")
    val payFrequencyType: String,

    @field:Min(value = 0, message = "Reliability must be positive")
    val reliabilityLevel: Int,

    @field:Min(value = 0, message = "Tax rate must be positive")
    val taxRate: Double,

    @field:Min(value = 0, message = "Min rate must be positive")
    val otherRate: Double,

    @field:NotNull(message = "Start date must be set")
    val startDate: LocalDate,

    @field:NotNull(message = "End date must be set")
    val endDate: LocalDate?,

    val linkedAccountId: UUID?,
    val annualGrossAmount: Double?
)

data class ApiUpdateIncomeSourceModel(
    val title: String?,
    val type: String?,
    val payFrequencyType: String?,

    @field:Min(0,"Reliability be positive or zero")
    val reliabilityLevel: Int?,

    @field:Min(0,"Tax rate be positive or zero")
    val taxRate: Double?,

    @field:Min(0,"Other rate be positive or zero")
    val otherRate: Double?,

    val startDate: LocalDate?,
    val endDate: LocalDate?,
    val linkedAccountId: UUID?,
    val annualGrossAmount: Double?
)

fun mapApiCreateIncomeSourceTo(model: ApiCreateIncomeSourceModel) : CreateIncomeSourceInput {
    return CreateIncomeSourceInput(
        title = model.title,
        type = IncomeSourceType.fromString(model.type),
        payFrequencyType = IncomeSourceFrequencyType.fromString(model.payFrequencyType),
        reliabilityLevel = model.reliabilityLevel,
        taxRate = model.taxRate,
        otherRate = model.otherRate,
        startDate = model.startDate,
        linkedAccountId = model.linkedAccountId,
        annualGrossAmount = model.annualGrossAmount,
        endDate = model.endDate
    )
}

fun mapApiUpdateIncomeSourceTo(id: UUID, model: ApiUpdateIncomeSourceModel) : UpdateIncomeSourceInput {
    return UpdateIncomeSourceInput(
        id = id,
        title = model.title,
        type = model.type?.let { IncomeSourceType.fromString(it) },
        payFrequencyType = model.payFrequencyType?.let { IncomeSourceFrequencyType.fromString(it) },
        reliabilityLevel = model.reliabilityLevel,
        taxRate = model.taxRate,
        otherRate = model.otherRate,
        startDate = model.startDate,
        linkedAccountId = model.linkedAccountId,
        annualGrossAmount = model.annualGrossAmount,
        endDate = model.endDate
    )
}