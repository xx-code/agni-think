package dev.auguste.agni_api.core.usecases.internal_loan.dto

import java.time.LocalDate
import java.util.UUID

data class GetInternalLoanOutput(
    val id: UUID,
    val creditTargetId: UUID,
    val invoiceId: UUID,
    val fundSourceId: UUID,
    val dueDate: LocalDate
)