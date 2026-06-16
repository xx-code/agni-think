package dev.auguste.agni_api.core.usecases.internal_loan.dto

import java.util.UUID

data class RemoveRefundInternalLoanInput(
    val internalLoanId: UUID,
    val freezeInvoiceId: UUID
)
