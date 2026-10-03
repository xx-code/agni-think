package usecases.invoices.dto

import domain.enums.InvoiceMovementType
import domain.enums.InvoiceType
import java.time.LocalDateTime
import java.util.UUID

data class UpdateInvoiceInput(
    val id: UUID,
    val accountId: UUID?,
    val date: LocalDateTime?,
    val type: InvoiceType?,
    val mouvementType: InvoiceMovementType?,
    val deductions: Set<InvoiceDeductionInput>?,
    val currency: UUID?,
    val removeTransactionIds: Set<UUID>,
    val addTransactions: Set<TransactionInput>,
)
