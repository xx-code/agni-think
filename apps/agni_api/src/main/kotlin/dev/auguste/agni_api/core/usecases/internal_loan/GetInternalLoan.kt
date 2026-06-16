package dev.auguste.agni_api.core.usecases.internal_loan

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.DomainException
import dev.auguste.agni_api.core.entities.InternalLoan
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.internal_loan.dto.GetInternalLoanOutput
import dev.auguste.agni_api.core.usecases.invoices.dto.GetBalanceInput
import dev.auguste.agni_api.core.usecases.invoices.dto.GetBalanceOutput
import dev.auguste.agni_api.core.usecases.invoices.dto.GetInvoiceOutput
import java.util.UUID

class GetInternalLoan(
    private val internalLoanRepo: IRepository<InternalLoan>,
    private val getInvoice: IUseCase<UUID, GetInvoiceOutput>
) : IUseCase<UUID, GetInternalLoanOutput> {
    override fun execAsync(input: UUID): GetInternalLoanOutput {
        val internalLoan = internalLoanRepo.get(input) ?: throw DomainException.NotFound.InternalLoan(input)

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