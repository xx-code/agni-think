package dev.auguste.agni_api.core.usecases.internal_loan.dto

import java.util.UUID

data class AddRefundInternalLoanInput(
    val internalLoanId: UUID,
    val accountId: UUID,
    val amount: Double
)
