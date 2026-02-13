package dev.auguste.agni_api.core.usecases.invoices.dto

import dev.auguste.agni_api.core.entities.enums.InvoiceMouvementType
import dev.auguste.agni_api.core.entities.enums.InvoiceStatusType
import dev.auguste.agni_api.core.entities.enums.InvoiceType
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Date
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
    val accountId: UUID,
    val status: InvoiceStatusType,
    val date: LocalDateTime,
    val type: InvoiceType,
    val mouvementType: InvoiceMouvementType,
    val currency: UUID?,
    val transactions: Set<TransactionInput>,
    val deductions: Set<InvoiceDeductionInput>,
    val isFreeze: Boolean = false,
    )
