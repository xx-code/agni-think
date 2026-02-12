package dev.auguste.agni_api.core.usecases.invoices.transactions.dto

import java.util.UUID

data class GetInvoiceTransactionsInput(
    val invoiceIds: Set<UUID>,
    val categoryIds: Set<UUID>?,
    val tagIds: Set<UUID>?,
    val budgetIds: Set<UUID>?,
    val minAmount: Double?,
    val maxAmount: Double?,
    val doRemoveSpecialCategory: Boolean? = false,
)
