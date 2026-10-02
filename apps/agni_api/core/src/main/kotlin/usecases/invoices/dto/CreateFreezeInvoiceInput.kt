package usecases.invoices.dto

import domain.enums.InvoiceStatusType
import java.time.LocalDateTime
import java.util.UUID

data class CreateFreezeInvoiceInput(
    val accountId: UUID,
    val endDate: LocalDateTime,
    val title: String,
    val amount: Double,
    val status: domain.enums.InvoiceStatusType
)