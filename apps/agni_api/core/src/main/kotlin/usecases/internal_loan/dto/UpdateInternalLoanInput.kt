package usecases.internal_loan.dto

import java.time.LocalDate
import java.util.UUID

data class UpdateInternalLoanInput(
    val id: UUID,
    val fundSourceId: UUID?,
    val dueDate: LocalDate?
)
