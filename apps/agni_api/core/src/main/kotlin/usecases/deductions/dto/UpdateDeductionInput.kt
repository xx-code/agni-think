package usecases.deductions.dto

import java.util.UUID

data class UpdateDeductionInput(
    val id: UUID,
    val title: String?,
    val description: String?
)
