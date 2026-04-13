package dev.auguste.agni_api.core.usecases.invoices.transactions.dto

import java.util.UUID

data class GetInvoiceTransactionsInput(
    val invoiceIds: Set<UUID>,
    val categoryIds: Set<UUID>? = null,
    val tagIds: Set<UUID>? = null,
    val budgetIds: Set<UUID>? = null,
    val minAmount: Double? = null,
    val maxAmount: Double? = null,
    val doRemoveSpecialCategory: Boolean? = false,
)
