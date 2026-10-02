package usecases.internal_loan

import adapters.repositories.IRepository
import domain.exceptions.NotFoundException
import domain.entities.InternalLoan
import usecases.interfaces.IUseCase
import usecases.internal_loan.dto.GetInternalLoanOutput
import usecases.invoices.dto.GetInvoiceOutput
import java.util.UUID

class GetInternalLoan(
    private val internalLoanRepo: IRepository<InternalLoan>,
    private val getInvoice: IUseCase<UUID, GetInvoiceOutput>
) : IUseCase<UUID, GetInternalLoanOutput> {
    override fun execAsync(input: UUID): GetInternalLoanOutput {
        val internalLoan = internalLoanRepo.get(input) ?: throw NotFoundException.SingleEntity(input, "internal_loan")

        val invoiceLoan = getInvoice.execAsync(internalLoan.invoiceId)

        var totalRefund = 0.0
        for(refundId in internalLoan.trackRefunds) {
            val refund = getInvoice.execAsync(refundId)
            totalRefund += refund.total
        }

        return GetInternalLoanOutput(
            id = internalLoan.id,
            creditTargetId = internalLoan.creditTargetId,
            invoiceId = internalLoan.invoiceId,
            fundSourceId = internalLoan.fundSourceId,
            dueDate = internalLoan.dueDate,
            loanAmount = invoiceLoan.total,
            refundAmount = totalRefund,
            freezeInvoices = internalLoan.trackRefunds.toList()
        )
    }
}