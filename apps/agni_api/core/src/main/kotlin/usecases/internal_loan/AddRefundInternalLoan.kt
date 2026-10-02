package usecases.internal_loan

import domain.FREEZE_CATEGORY_ID
import adapters.repositories.IRepository
import adapters.repositories.IUnitOfWork
import domain.exceptions.NotFoundException
import domain.exceptions.ValidationException
import domain.entities.InternalLoan
import domain.enums.InvoiceMovementType
import domain.enums.InvoiceStatusType
import domain.enums.InvoiceType
import usecases.CreatedOutput
import usecases.interfaces.IInnerUseCase
import usecases.interfaces.IUseCase
import usecases.internal_loan.dto.AddRefundInternalLoanInput
import usecases.invoices.dto.CreateInvoiceInput
import usecases.invoices.dto.GetInvoiceOutput
import usecases.invoices.dto.TransactionInput
import java.util.UUID

class AddRefundInternalLoan(
    private val internalLoanRepo: IRepository<InternalLoan>,
    private val getInvoice: IUseCase<UUID, GetInvoiceOutput>,
    private val createInvoice: IInnerUseCase<CreateInvoiceInput, CreatedOutput>,
    private val unitOfWork: IUnitOfWork
): IUseCase<AddRefundInternalLoanInput, Unit> {
    override fun execAsync(input: AddRefundInternalLoanInput) {
        unitOfWork.execute {
            val internalLoan = internalLoanRepo.get(input.internalLoanId) ?: throw NotFoundException.SingleEntity(input.internalLoanId, "internal_loan")
            val invoiceLoan = getInvoice.execAsync(internalLoan.invoiceId)


            var totalRefund = 0.0
            for(refundId in internalLoan.trackRefunds) {
                val refund = getInvoice.execAsync(refundId)
                totalRefund += refund.total
            }

            val internalLoanRemind = invoiceLoan.total - totalRefund

            if (input.amount > internalLoanRemind)
                throw ValidationException.InternalLoanRefundNotValid(input.amount, internalLoanRemind)

            val resFreeze = createInvoice.execInnerAsync(CreateInvoiceInput(
                accountId = input.accountId,
                status = _root_ide_package_.domain.enums.InvoiceStatusType.COMPLETED,
                date = internalLoan.dueDate.atStartOfDay(),
                type = _root_ide_package_.domain.enums.InvoiceType.OTHER,
                mouvementType = _root_ide_package_.domain.enums.InvoiceMovementType.DEBIT,
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
}