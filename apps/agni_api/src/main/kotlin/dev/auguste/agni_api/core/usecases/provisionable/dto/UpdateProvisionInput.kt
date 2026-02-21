package dev.auguste.agni_api.core.usecases.provisionable.dto

import java.time.LocalDate
import java.util.UUID

data class UpdateProvisionInput(
    val id: UUID,
    val title: String?,
    val initialCost: Double?,
    val acquisitionDate: LocalDate?,
    val expectedLifespanMonth: Int?,
    val residualValue: Double?
)