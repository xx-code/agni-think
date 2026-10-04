package usecases.internal_loan

import usecases.interfaces.IUseCase

import domain.FREEZE_CATEGORY_ID
import adapters.repositories.IRepository
import adapters.repositories.IUnitOfWork
import domain.exceptions.NotFoundException
import domain.exceptions.ValidationException
import domain.entities.InternalLoan
import domain.enums.InvoiceMovementType
import domain.enums.InvoiceStatusType
import domain.enums.InvoiceType
import usecases.UseCase
import usecases.dto.CreatedOutput
import usecases.internal_loan.dto.AddRefundInternalLoanInput
import usecases.invoices.dto.CreateInvoiceInput
import usecases.invoices.dto.GetInvoiceOutput
import usecases.invoices.dto.TransactionInput
import java.util.UUID

class AddRefundInternalLoan(
    private val internalLoanRepo: IRepository<InternalLoan>,
    private val getInvoice: IUseCase<UUID, GetInvoiceOutput>,
    private val createInvoice: IUseCase<CreateInvoiceInput, CreatedOutput>,
    unitOfWork: IUnitOfWork
): UseCase<AddRefundInternalLoanInput, Unit>(unitOfWork) {
    override suspend fun process(input: AddRefundInternalLoanInput) {
        val internalLoan = internalLoanRepo.get(input.internalLoanId) ?: throw NotFoundException.SingleEntity(input.internalLoanId, "internal_loan")
        val invoiceLoan = getInvoice.processDirect(internalLoan.invoiceId)


        var totalRefund = 0.0
        for(refundId in internalLoan.trackRefunds) {
            val refund = getInvoice.processDirect(refundId)
            totalRefund += refund.total
        }

        val internalLoanRemind = invoiceLoan.total - totalRefund

        if (input.amount > internalLoanRemind)
            throw ValidationException.InternalLoanRefundNotValid(input.amount, internalLoanRemind)

        val resFreeze = createInvoice.processDirect(CreateInvoiceInput(
            accountId = input.accountId,
            status = InvoiceStatusType.COMPLETED,
            date = internalLoan.dueDate.atStartOfDay(),
            type = InvoiceType.OTHER,
            mouvementType = InvoiceMovementType.DEBIT,
            currency = null,
            isFreeze = true,
            transactions = setOf(
                TransactionInput(
                    amount = input.amount,
                    categoryId = FREEZE_CATEGORY_ID,
                    description = "Freeze pour internal loan ${invoiceLoan.transactions.first().description}",
                    tagIds = setOf(),
                    budgetIds = setOf()
                )
            ),
            deductions = setOf()
        ))

        val refunds = internalLoan.trackRefunds.toMutableSet()
        refunds.add(resFreeze.newId)

        internalLoan.trackRefunds = refunds
        internalLoanRepo.update(internalLoan)
    }
}