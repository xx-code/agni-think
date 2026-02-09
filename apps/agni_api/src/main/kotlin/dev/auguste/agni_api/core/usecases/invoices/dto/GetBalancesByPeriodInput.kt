package dev.auguste.agni_api.core.usecases.invoices.dto

import dev.auguste.agni_api.core.entities.enums.InvoiceMouvementType
import dev.auguste.agni_api.core.entities.enums.InvoiceStatusType
import dev.auguste.agni_api.core.entities.enums.InvoiceType
import dev.auguste.agni_api.core.entities.enums.PeriodType
import java.time.LocalDateTime
import java.util.Date
import java.util.UUID

data class GetBalancesByPeriodInput(
    val period: PeriodType,
    val interval: Int,
    val dateFrom: LocalDateTime,
    val status: InvoiceStatusType? = null,
    val dateTo: LocalDateTime? = null,
    val accountIds: Set<UUID>? = null,
    val mouvement: InvoiceMouvementType? = null,
    val types: Set<InvoiceType>? = null,
    val isFreeze: Boolean? = null,
    val categoryIds: Set<UUID>? = null,
    val tagIds: Set<UUID>? = null,
    val budgetIds: Set<UUID>? = null,
    val minAmount: Double? = null,
    val maxAmount: Double? = null
)
