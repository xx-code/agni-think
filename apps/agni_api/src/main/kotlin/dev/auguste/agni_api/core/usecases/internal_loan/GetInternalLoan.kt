package dev.auguste.agni_api.core.usecases.internal_loan

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.InternalLoan
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.internal_loan.dto.GetInternalLoanOutput
import java.util.UUID

class GetInternalLoan(
    private val internalLoanRepo: IRepository<InternalLoan>
) : IUseCase<UUID, GetInternalLoanOutput> {
    override fun execAsync(input: UUID): GetInternalLoanOutput {
        val internalLoan = internalLoanRepo.get(input) ?: throw Exception("Internal loan $input not found")

        return GetInternalLoanOutput(
            id = internalLoan.id,
            creditTargetId = internalLoan.creditTargetId,
            invoiceId = internalLoan.invoiceId,
            fundSourceId = internalLoan.fundSourceId,
            dueDate = internalLoan.dueDate
        )
    }
}