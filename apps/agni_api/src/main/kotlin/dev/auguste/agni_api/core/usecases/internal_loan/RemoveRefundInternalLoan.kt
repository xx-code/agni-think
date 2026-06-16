package dev.auguste.agni_api.core.usecases.internal_loan

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.adapters.repositories.IUnitOfWork
import dev.auguste.agni_api.core.entities.DomainException
import dev.auguste.agni_api.core.entities.InternalLoan
import dev.auguste.agni_api.core.usecases.interfaces.IInnerUseCase
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.internal_loan.dto.RemoveRefundInternalLoanInput
import dev.auguste.agni_api.core.usecases.invoices.dto.DeleteInvoiceInput
import dev.auguste.agni_api.core.usecases.invoices.dto.GetInvoiceOutput
import java.util.UUID

class RemoveRefundInternalLoan(
    private val internalLoanRepo: IRepository<InternalLoan>,
    private val getInvoice: IUseCase<UUID, GetInvoiceOutput>,
    private val deleteInvoice: IInnerUseCase<DeleteInvoiceInput, Unit>,
    private val unitOfWork: IUnitOfWork
) : IUseCase<RemoveRefundInternalLoanInput, Unit>{
    override fun execAsync(input: RemoveRefundInternalLoanInput) {
        unitOfWork.execute {
            val internalLoan = internalLoanRepo.get(input.internalLoanId) ?: throw DomainException.NotFound.InternalLoan(input.internalLoanId)
            val invoiceLoan = getInvoice.execAsync(internalLoan.invoiceId)

            if (internalLoan.trackRefunds.find({ it == input.freezeInvoiceId}) == null)
                throw DomainException.NotFound.InternalLoanFreezeInvoice(input.internalLoanId)

            val refunds = internalLoan.trackRefunds.toMutableSet()
            refunds.remove(input.freezeInvoiceId)
            internalLoan.trackRefunds = refunds

            deleteInvoice.execInnerAsync(DeleteInvoiceInput(invoiceLoan.id, false))

            internalLoanRepo.update(internalLoan)
        }
    }
}