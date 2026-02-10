package dev.auguste.agni_api.core.usecases.invoices.dto

import dev.auguste.agni_api.core.entities.enums.InvoiceMouvementType
import dev.auguste.agni_api.core.entities.enums.InvoiceStatusType
import dev.auguste.agni_api.core.usecases.invoices.transactions.dto.TransactionOutput
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Date
import java.util.UUID

data class InvoiceDeductionOutput(
    val id: UUID,
    val amount: Double
)

data class GetInvoiceOutput(
    val id: UUID,
    val accountId: UUID,
    val status: InvoiceStatusType,
    val subTotal: Double,
    val total: Double,
    val mouvement: InvoiceMouvementType,
    val date: LocalDateTime,
    val transactions: List<TransactionOutput>,
    val deductions: List<InvoiceDeductionOutput>
)