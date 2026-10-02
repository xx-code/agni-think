package usecases.finance_principles

import adapters.repositories.IRepository
import domain.exceptions.NotFoundException
import domain.entities.FinancePrinciple
import usecases.finance_principles.dto.DeleteFinancePrincipleInput
import usecases.interfaces.IUseCase

class DeleteFinancePrinciple(
    private val financePrincipleRepo: IRepository<FinancePrinciple>
) : IUseCase<DeleteFinancePrincipleInput, Unit> {
    override fun execAsync(input: DeleteFinancePrincipleInput) {
        financePrincipleRepo.get(input.principalId) ?: throw NotFoundException.SingleEntity(input.principalId, "finance_principle")

        financePrincipleRepo.delete(input.principalId)
    }
}