package usecases.internal_loan

import adapters.repositories.IRepository
import adapters.repositories.IUnitOfWork
import domain.exceptions.NotFoundException
import domain.entities.InternalLoan
import usecases.interfaces.IInnerUseCase
import usecases.interfaces.IUseCase
import usecases.invoices.dto.DeleteInvoiceInput
import java.util.UUID

class DeleteInternalLoan(
    private val internalLoanRepo: IRepository<InternalLoan>,
    private val deleteInvoice: IInnerUseCase<DeleteInvoiceInput, Unit>,
    private val unitOfWork: IUnitOfWork
) : IUseCase<UUID, Unit> {
    override fun execAsync(input: UUID) {
        unitOfWork.execute {
            val internalLoan = internalLoanRepo.get(input) ?: throw NotFoundException.SingleEntity(input, "internal_loan")
            internalLoanRepo.delete(input)
            deleteInvoice.execInnerAsync(DeleteInvoiceInput(internalLoan.invoiceId, false))

            for (freezeId in internalLoan.trackRefunds) {
                deleteInvoice.execAsync(DeleteInvoiceInput(freezeId, false))
            }
        }
    }
}