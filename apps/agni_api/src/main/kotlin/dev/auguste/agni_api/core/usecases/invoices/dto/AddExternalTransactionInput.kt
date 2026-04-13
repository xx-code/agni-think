package dev.auguste.agni_api.core.usecases.invoices.dto

import java.time.LocalDateTime

data class AddExternalTransactionInput(
    val accountId: String,
    val transactionId: String,
    val amount: Double,
    val dateTransaction: LocalDateTime,
    val merchantName: String,
    val categoryPrimary: String,
    val categoryDetail: String,
    val isTreated: Boolean
)
