package dev.auguste.agni_api.core.usecases.budgets

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Budget
import dev.auguste.agni_api.core.usecases.budgets.dto.DeleteBudgetInput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase

class DeleteBudget(
    private val budgetRepo: IRepository<Budget>
): IUseCase<DeleteBudgetInput, Unit>{
    override fun execAsync(input: DeleteBudgetInput) {
        budgetRepo.get(input.budgetId) ?: throw Error("Budget ID ${input.budgetId} not found")
        budgetRepo.delete(input.budgetId)
    }
}