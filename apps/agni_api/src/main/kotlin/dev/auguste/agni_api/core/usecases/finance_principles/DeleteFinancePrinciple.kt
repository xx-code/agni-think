package dev.auguste.agni_api.core.usecases.finance_principles

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.FinancePrinciple
import dev.auguste.agni_api.core.usecases.finance_principles.dto.DeleteFinancePrincipleInput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase

class DeleteFinancePrinciple(
    private val financePrincipleRepo: IRepository<FinancePrinciple>
) : IUseCase<DeleteFinancePrincipleInput, Unit> {
    override fun execAsync(input: DeleteFinancePrincipleInput) {
        financePrincipleRepo.get(input.principalId) ?: throw Error("FinancePrinciple ${input.principalId} not found")

        financePrincipleRepo.delete(input.principalId)
    }
}