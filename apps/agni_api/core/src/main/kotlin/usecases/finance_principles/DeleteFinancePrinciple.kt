package usecases.finance_principles

import adapters.IAuthenticateAdapter
import usecases.UseCase
import adapters.repositories.IRepository
import domain.exceptions.NotFoundException
import domain.entities.FinancePrinciple
import usecases.finance_principles.dto.DeleteFinancePrincipleInput
class DeleteFinancePrinciple(
    private val financePrincipleRepo: IRepository<FinancePrinciple>,
    auth: IAuthenticateAdapter
): UseCase<DeleteFinancePrincipleInput, Unit>(auth = auth) {
    override suspend fun process(input: DeleteFinancePrincipleInput) {
        financePrincipleRepo.get(input.principalId) ?: throw NotFoundException.SingleEntity(input.principalId, "finance_principle")

        financePrincipleRepo.delete(input.principalId)
    }
}