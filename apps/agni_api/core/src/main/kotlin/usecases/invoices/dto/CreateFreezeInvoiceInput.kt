package usecases.invoices.dto

import domain.enums.InvoiceStatusType
import java.time.LocalDateTime
import java.util.UUID

data class CreateFreezeInvoiceInput(
    val userId: UUID,
    val accountId: UUID,
    val endDate: LocalDateTime,
    val title: String,
    val amount: Double,
    val status: InvoiceStatusType
)