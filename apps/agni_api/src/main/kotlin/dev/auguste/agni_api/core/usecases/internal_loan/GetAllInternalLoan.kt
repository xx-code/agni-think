package dev.auguste.agni_api.core.usecases.internal_loan

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Account
import dev.auguste.agni_api.core.entities.InternalLoan
import dev.auguste.agni_api.core.entities.Invoice
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.internal_loan.dto.GetInternalLoanOutput

class GetAllInternalLoan(
    private val internalLoanRepo: IRepository<InternalLoan>,
): IUseCase<QueryFilter, ListOutput<GetInternalLoanOutput>> {
    override fun execAsync(input: QueryFilter): ListOutput<GetInternalLoanOutput> {
        val internalLoans = internalLoanRepo.getAll(input)

        return ListOutput(
            items = internalLoans.items.map {
                GetInternalLoanOutput(
                    id = it.id,
                    creditTargetId = it.creditTargetId,
                    invoiceId = it.invoiceId,
                    fundSourceId = it.fundSourceId,
                    dueDate = it.dueDate,
                )
            },
            total = internalLoans.total
        )
    }
}