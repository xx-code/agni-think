package dev.auguste.agni_api.core.usecases.deductions.dto

import dev.auguste.agni_api.core.entities.enums.DeductionBaseType
import dev.auguste.agni_api.core.entities.enums.DeductionModeType
import java.util.UUID

data class GetDeductionOutput(
    val id: UUID,
    val title: String,
    val description: String,
    val base: String,
    val mode: String
)
