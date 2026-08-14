package dev.auguste.agni_api.core.usecases.invoices.dto

import dev.auguste.agni_api.core.usecases.invoices.transactions.dto.TransactionOutput
import java.time.LocalDateTime
import java.util.UUID

data class InvoiceDeductionOutput(
    val id: UUID,
    val amount: Double
)

data class GetInvoiceOutput(
    val id: UUID,
    val accountId: UUID,
    val status: String,
    val subTotal: Double,
    val total: Double,
    val type: String,
    val mouvement: String,
    val date: LocalDateTime,
    val transactions: List<TransactionOutput>,
    val deductions: List<InvoiceDeductionOutput>
)