package usecases.invoices.dto

import adapters.dto.QueryFilter
import domain.enums.InvoiceMovementType
import domain.enums.InvoiceStatusType
import domain.enums.InvoiceType
import java.time.LocalDateTime
import java.util.UUID

data class GetAllInvoiceInput(
    val queryFilter: QueryFilter,
    val accountIds: Set<UUID>? = null,
    val startDate: LocalDateTime? = null,
    val endDate: LocalDateTime? = null,
    val status: InvoiceStatusType? = null,
    val types: Set<InvoiceType>? = null,
    val isFreeze: Boolean? = null,
    val movementType: InvoiceMovementType? = null,
    val categoryIds: Set<UUID>? = null,
    val tagIds: Set<UUID>? = null,
    val budgetIds: Set<UUID>? = null,
    val minAmount: Double? = null,
    val maxAmount: Double? = null
)