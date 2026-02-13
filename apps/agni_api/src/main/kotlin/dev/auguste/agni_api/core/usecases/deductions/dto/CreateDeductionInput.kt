package dev.auguste.agni_api.core.usecases.deductions.dto

import dev.auguste.agni_api.core.entities.enums.DeductionBaseType
import dev.auguste.agni_api.core.entities.enums.DeductionModeType

data class CreateDeductionInput(
    val title: String,
    val description: String,
    val base: DeductionBaseType,
    val mode: DeductionModeType
)
