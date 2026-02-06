package dev.auguste.agni_api.core.usecases.invoices.dto

import java.time.LocalDateTime
import java.util.Date
import java.util.UUID

data class TransfertInvoiceInput(
    val accountIdFrom: UUID,
    val accountIdTo: UUID,
    val date: LocalDateTime,
    val amount: Double
)
