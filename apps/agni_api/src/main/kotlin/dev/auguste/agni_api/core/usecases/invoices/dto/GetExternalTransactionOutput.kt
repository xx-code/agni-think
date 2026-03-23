package dev.auguste.agni_api.core.usecases.invoices.dto

import java.time.LocalDateTime
import java.util.UUID

data class GetExternalTransactionOutput(
    val id: UUID,
    val accountId: String,
    val amount: Double,
    val dateTransaction: LocalDateTime,
    val merchantName: String,
    val categoryPrimary: String,
    val categoryDetail: String,
    val isTreated: Boolean
)