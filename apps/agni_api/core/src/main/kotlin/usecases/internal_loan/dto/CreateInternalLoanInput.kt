package usecases.internal_loan.dto

import usecases.invoices.dto.CreateInvoiceInput
import java.time.LocalDate
import java.util.UUID

data class CreateInternalLoanInput(
    val creditTargetId: UUID,
    val fundSourceId: UUID,
    val invoiceInput: CreateInvoiceInput,
    val dueDate: LocalDate,
)