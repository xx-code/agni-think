package dev.auguste.agni_api.core.usecases.internal_loan

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.adapters.repositories.IUnitOfWork
import dev.auguste.agni_api.core.entities.DomainException
import dev.auguste.agni_api.core.entities.InternalLoan
import dev.auguste.agni_api.core.usecases.interfaces.IInnerUseCase
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.invoices.dto.DeleteInvoiceInput
import java.util.UUID

class DeleteInternalLoan(
    private val internalLoanRepo: IRepository<InternalLoan>,
    private val deleteInvoice: IInnerUseCase<DeleteInvoiceInput, Unit>,
    private val unitOfWork: IUnitOfWork
) : IUseCase<UUID, Unit> {
    override fun execAsync(input: UUID) {
        unitOfWork.execute {
            val internalLoan = internalLoanRepo.get(input) ?: throw DomainException.NotFound.InternalLoan(input)
            internalLoanRepo.delete(input)
            deleteInvoice.execInnerAsync(DeleteInvoiceInput(internalLoan.invoiceId, false))

            for (freezeId in internalLoan.trackRefunds) {
                deleteInvoice.execAsync(DeleteInvoiceInput(freezeId, false))
            }
        }
    }
}