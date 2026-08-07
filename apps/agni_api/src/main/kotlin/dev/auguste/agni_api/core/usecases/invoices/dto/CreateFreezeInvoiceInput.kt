package dev.auguste.agni_api.core.usecases.invoices.dto

import dev.auguste.agni_api.core.entities.enums.InvoiceStatusType
import java.time.LocalDateTime
import java.util.UUID

data class CreateFreezeInvoiceInput(
    val accountId: UUID,
    val endDate: LocalDateTime,
    val title: String,
    val amount: Double,
    val status: InvoiceStatusType
)