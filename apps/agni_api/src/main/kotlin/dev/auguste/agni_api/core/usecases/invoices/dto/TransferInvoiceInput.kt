package dev.auguste.agni_api.core.usecases.invoices.dto

import java.time.LocalDateTime
import java.util.UUID

data class TransferInvoiceInput(
    val accountIdFrom: UUID,
    val accountIdTo: UUID,
    val date: LocalDateTime,
    val amount: Double
)
