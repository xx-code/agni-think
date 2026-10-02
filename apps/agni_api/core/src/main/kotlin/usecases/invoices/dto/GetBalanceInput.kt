package usecases.invoices.dto

import domain.enums.InvoiceMovementType
import domain.enums.InvoiceStatusType
import domain.enums.InvoiceType
import java.time.LocalDateTime
import java.util.UUID

data class GetBalanceInput(
    val accountIds: Set<UUID>? = null,
    val status: domain.enums.InvoiceStatusType? = _root_ide_package_.domain.enums.InvoiceStatusType.COMPLETED,
    val startDate: LocalDateTime? = null,
    val endDate: LocalDateTime? = null,
    val types: Set<domain.enums.InvoiceType>? = null,
    val isFreeze: Boolean? = null,
    val mouvement: domain.enums.InvoiceMovementType? = null,
    val categoryIds: Set<UUID>? = null,
    val tagIds: Set<UUID>? = null,
    val budgetIds: Set<UUID>? = null,
    val minAmount: Double? = null,
    val maxAmount: Double? = null,
    val removeSystemCategory: Boolean? = true,
)