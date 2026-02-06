package dev.auguste.agni_api.core.usecases.invoices.transactions.dto

import java.util.UUID

data class TransactionOutput(
    val id: UUID,
    val amount: Double,
    val description: String,
    val categoryId: UUID,
    val tagIds: Set<UUID>,
    val budgetIds: Set<UUID>
)

data class GetInvoiceTransactionsOutput(
    val invoiceId: UUID,
    val transactions: List<TransactionOutput>,
    val total: Double,
    val subTotal: Double
)
