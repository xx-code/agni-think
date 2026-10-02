package usecases.deductions.dto

import domain.enums.DeductionBaseType
import domain.enums.DeductionModeType

data class CreateDeductionInput(
    val title: String,
    val description: String,
    val base: domain.enums.DeductionBaseType,
    val mode: domain.enums.DeductionModeType
)
