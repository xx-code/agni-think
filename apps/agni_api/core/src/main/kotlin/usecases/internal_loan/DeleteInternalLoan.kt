package usecases.internal_loan

import usecases.interfaces.IUseCase

import adapters.repositories.IRepository
import adapters.repositories.IUnitOfWork
import domain.exceptions.NotFoundException
import domain.entities.InternalLoan
import usecases.UseCase
import usecases.invoices.dto.DeleteInvoiceInput
import java.util.UUID

class DeleteInternalLoan(
    private val internalLoanRepo: IRepository<InternalLoan>,
    private val deleteInvoice: IUseCase<DeleteInvoiceInput, Unit>,
    unitOfWork: IUnitOfWork
): UseCase<UUID, Unit>(unitOfWork) {
    override suspend fun process(input: UUID) {
        val internalLoan = internalLoanRepo.get(input) ?: throw NotFoundException.SingleEntity(input, "internal_loan")
        internalLoanRepo.delete(input)
        deleteInvoice.processDirect(DeleteInvoiceInput(internalLoan.invoiceId, false))

        for (freezeId in internalLoan.trackRefunds) {
            deleteInvoice.processDirect(DeleteInvoiceInput(freezeId, false))
        }
    }
}