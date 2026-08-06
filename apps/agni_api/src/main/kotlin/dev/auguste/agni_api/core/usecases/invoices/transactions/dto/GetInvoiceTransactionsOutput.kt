package dev.auguste.agni_api.core.usecases.invoices.transactions.dto

import java.util.UUID

data class TransactionTagOutput(
    val id: UUID,
    val value: String,
    val color: String
)

data class TransactionCategoryOutput(
    val id: UUID,
    val title: String,
    val icon: String,
    val color: String
)

data class TransactionBudgetOutput(
    val id: UUID,
    val value: String
)

data class TransactionOutput(
    val id: UUID,
    val amount: Double,
    val description: String,
    val category: TransactionCategoryOutput,
    val tags: Set<TransactionTagOutput>,
    val budgets: Set<TransactionBudgetOutput>
)

data class GetInvoiceTransactionsOutput(
    val invoiceId: UUID,
    val transactions: List<TransactionOutput>,
    val total: Double,
    val subTotal: Double
)
