package usecases.deductions.dto

import domain.enums.DeductionBaseType
import domain.enums.DeductionModeType

data class CreateDeductionInput(
    val title: String,
    val description: String,
    val base: DeductionBaseType,
    val mode: DeductionModeType
)
