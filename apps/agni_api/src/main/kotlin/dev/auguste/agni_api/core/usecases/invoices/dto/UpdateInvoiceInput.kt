package dev.auguste.agni_api.core.usecases.invoices.dto

import dev.auguste.agni_api.core.entities.enums.InvoiceMouvementType
import dev.auguste.agni_api.core.entities.enums.InvoiceStatusType
import dev.auguste.agni_api.core.entities.enums.InvoiceType
import java.time.LocalDateTime
import java.util.UUID

data class UpdateInvoiceInput(
    val id: UUID,
    val accountId: UUID?,
    val status: InvoiceStatusType?,
    val date: LocalDateTime?,
    val type: InvoiceType?,
    val mouvementType: InvoiceMouvementType?,
    val deductions: Set<InvoiceDeductionInput>?,
    val currency: UUID?,
    val removeTransactionIds: Set<UUID>,
    val addTransactions: Set<TransactionInput>,
)
