package dev.auguste.agni_api.core.usecases.budgets

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Budget
import dev.auguste.agni_api.core.usecases.budgets.dto.DeleteBudgetInput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.entities.DomainException

class DeleteBudget(
    private val budgetRepo: IRepository<Budget>
): IUseCase<DeleteBudgetInput, Unit>{
    override fun execAsync(input: DeleteBudgetInput) {
        budgetRepo.get(input.budgetId) ?: throw DomainException.NotFound.Budget(input.budgetId)
        budgetRepo.delete(input.budgetId)
    }
}