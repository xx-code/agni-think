package usecases.internal_loan

import adapters.repositories.IRepository
import adapters.repositories.IUnitOfWork
import domain.exceptions.NotFoundException
import domain.entities.InternalLoan
import usecases.interfaces.IInnerUseCase
import usecases.interfaces.IUseCase
import usecases.internal_loan.dto.RemoveRefundInternalLoanInput
import usecases.invoices.dto.DeleteInvoiceInput
import usecases.invoices.dto.GetInvoiceOutput
import java.util.UUID

class RemoveRefundInternalLoan(
    private val internalLoanRepo: IRepository<InternalLoan>,
    private val getInvoice: IUseCase<UUID, GetInvoiceOutput>,
    private val deleteInvoice: IInnerUseCase<DeleteInvoiceInput, Unit>,
    private val unitOfWork: IUnitOfWork
) : IUseCase<RemoveRefundInternalLoanInput, Unit>{
    override fun execAsync(input: RemoveRefundInternalLoanInput) {
        unitOfWork.execute {
            val internalLoan = internalLoanRepo.get(input.internalLoanId) ?: throw NotFoundException.SingleEntity(input.internalLoanId, "internal_loan")

            if (internalLoan.trackRefunds.find({ it == input.freezeInvoiceId}) == null)
                throw NotFoundException.SingleEntity(input.internalLoanId, "internal_loan_freeze_invoice")

            val refunds = internalLoan.trackRefunds.toMutableSet()
            refunds.remove(input.freezeInvoiceId)
            internalLoan.trackRefunds = refunds

            deleteInvoice.execInnerAsync(DeleteInvoiceInput(input.freezeInvoiceId, false))

            internalLoanRepo.update(internalLoan)
        }
    }
}