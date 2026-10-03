package usecases.internal_loan

import usecases.UseCase
import adapters.dto.QueryFilter
import adapters.repositories.IRepository
import domain.entities.InternalLoan
import usecases.dto.ListOutput
import usecases.interfaces.IUseCase
import usecases.internal_loan.dto.GetInternalLoanOutput
import usecases.invoices.dto.GetInvoiceOutput
import java.util.UUID

class GetAllInternalLoan(
    private val internalLoanRepo: IRepository<InternalLoan>,
    private val getInvoice: IUseCase<UUID, GetInvoiceOutput>
): UseCase<QueryFilter, ListOutput<GetInternalLoanOutput>>() {
    override suspend fun process(input: QueryFilter): ListOutput<GetInternalLoanOutput> {
        val internalLoans = internalLoanRepo.getAll(input)

        // TODO: Refactoring for optimization
        val results = mutableListOf<GetInternalLoanOutput>()
        for (internalLoan in internalLoans.items) {
            val invoiceLoan = getInvoice.processDirect(internalLoan.invoiceId)


            var totalRefund = 0.0
            for(refundId in internalLoan.trackRefunds) {
                val refund = getInvoice.processDirect(refundId)
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