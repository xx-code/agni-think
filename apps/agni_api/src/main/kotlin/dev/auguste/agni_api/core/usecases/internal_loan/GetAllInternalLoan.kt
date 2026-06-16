package dev.auguste.agni_api.core.usecases.internal_loan

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Account
import dev.auguste.agni_api.core.entities.InternalLoan
import dev.auguste.agni_api.core.entities.Invoice
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.internal_loan.dto.GetInternalLoanOutput
import dev.auguste.agni_api.core.usecases.invoices.dto.GetInvoiceOutput
import java.util.UUID

class GetAllInternalLoan(
    private val internalLoanRepo: IRepository<InternalLoan>,
    private val getInvoice: IUseCase<UUID, GetInvoiceOutput>
): IUseCase<QueryFilter, ListOutput<GetInternalLoanOutput>> {
    override fun execAsync(input: QueryFilter): ListOutput<GetInternalLoanOutput> {
        val internalLoans = internalLoanRepo.getAll(input)

        // TODO: Refactoring for optimization
        val results = mutableListOf<GetInternalLoanOutput>()
        for (internalLoan in internalLoans.items) {
            val invoiceLoan = getInvoice.execAsync(internalLoan.invoiceId)


            var totalRefund = 0.0
            for(refundId in internalLoan.trackRefunds) {
                val refund = getInvoice.execAsync(refundId)
                totalRefund += refund.total
            }

            results.add(
                GetInternalLoanOutput(
                    id = internalLoan.id,
                    creditTargetId = internalLoan.creditTargetId,
                    invoiceId = internalLoan.invoiceId,
                    fundSourceId = internalLoan.fundSourceId,
                    dueDate = internalLoan.dueDate,
                    loanAmount = invoiceLoan.total,
                    refundAmount = totalRefund,
                    freezeInvoices = internalLoan.trackRefunds.toList()
                )
            )
        }

        return ListOutput(
            items = results,
            total = internalLoans.total
        )
    }
}