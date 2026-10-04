package dev.auguste.rest_api.controllers.models

import domain.enums.InvoiceMovementType
import domain.enums.InvoiceStatusType
import domain.enums.InvoiceType
import usecases.internal_loan.dto.CreateInternalLoanInput
import usecases.invoices.dto.CreateInvoiceInput
import usecases.invoices.dto.InvoiceDeductionInput
import usecases.invoices.dto.TransactionInput
import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class ApiCreateInternalLoanModel(
    val fundAccountId: UUID,
    val creditAccountId: UUID,
    val transactions: List<ApiTransactionModel>,
    val deductions: List<ApiDeductionModel>,
    @field:DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    val transactionDate: LocalDateTime,
    val dueDate: LocalDate,
)

data class ApiUpdateInternalLoanModel(
    val fundAccountId: UUID? = null,
    val dueDate: LocalDate? = null
)

data class ApiAddRefundInternalLoanModel(
    val refundAccountId: UUID,
    val refundAmount: Double,
)

data class ApiRemoveRefundInternalLoanModel(
    val freezeInvoiceRefundId: UUID
)


fun mapApiCreateInternalLoanModel(input: ApiCreateInternalLoanModel): CreateInternalLoanInput {
    return CreateInternalLoanInput(
        creditTargetId = input.creditAccountId,
        fundSourceId = input.fundAccountId,
        invoiceInput = CreateInvoiceInput(
            accountId = input.creditAccountId,
            status = InvoiceStatusType.PENDING,
            date = input.transactionDate,
            type = InvoiceType.VARIABLE_COST,
            mouvementType = InvoiceMovementType.DEBIT,
            currency = null,
            transactions = input.transactions.map {
                TransactionInput(
                    amount = it.amount,
                    categoryId = it.categoryId,
                    description = it.description,
                    tagIds = it.tagIds,
                    budgetIds = it.budgetIds
                )
            }.toSet(),
            deductions = input.deductions.map {
                InvoiceDeductionInput(
                    deductionId = it.deductionId,
                    amount = it.amount
                )
            }.toSet(),
            isFreeze = false
        ),
        dueDate = input.dueDate
    )
}
