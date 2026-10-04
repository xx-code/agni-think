package usecases.invoices.dto

import domain.enums.InvoiceMovementType
import domain.enums.InvoiceStatusType
import domain.enums.InvoiceType
import domain.enums.PeriodType
import java.time.LocalDateTime
import java.util.UUID

data class GetBalancesByPeriodInput(
    val period: PeriodType,
    val interval: Int,
    val dateFrom: LocalDateTime,
    val status: InvoiceStatusType? = InvoiceStatusType.COMPLETED,
    val dateTo: LocalDateTime? = null,
    val accountIds: Set<UUID>? = null,
    val mouvement: InvoiceMovementType? = null,
    val types: Set<InvoiceType>? = null,
    val isFreeze: Boolean? = null,
    val categoryIds: Set<UUID>? = null,
    val tagIds: Set<UUID>? = null,
    val budgetIds: Set<UUID>? = null,
    val minAmount: Double? = null,
    val maxAmount: Double? = null,
    val removeSystemCategory: Boolean? = true,
)
