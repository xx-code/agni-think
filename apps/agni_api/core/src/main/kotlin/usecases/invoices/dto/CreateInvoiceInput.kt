package usecases.invoices.dto

import domain.enums.InvoiceMovementType
import domain.enums.InvoiceStatusType
import domain.enums.InvoiceType
import domain.value_objects.InvoiceModuleLinker
import java.time.LocalDateTime
import java.util.UUID

data class InvoiceDeductionInput(
    val deductionId: UUID,
    val amount: Double
)

data class TransactionInput(
    val amount: Double,
    val categoryId: UUID,
    val description: String,
    val tagIds: Set<UUID>,
    val budgetIds: Set<UUID>
)

data class CreateInvoiceInput(
    val persistentInvoiceId: UUID? = null,
    val accountId: UUID,
    val status: domain.enums.InvoiceStatusType,
    val date: LocalDateTime,
    val type: domain.enums.InvoiceType,
    val mouvementType: domain.enums.InvoiceMovementType,
    val currency: UUID?,
    val transactions: Set<TransactionInput>,
    val deductions: Set<InvoiceDeductionInput>,
    val moduleSourcesLinker: List<InvoiceModuleLinker> = emptyList(),
    val isFreeze: Boolean = false,
    )
