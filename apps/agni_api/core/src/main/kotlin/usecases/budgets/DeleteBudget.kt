package usecases.budgets

import usecases.UseCase
import adapters.repositories.IRepository
import domain.entities.Budget
import usecases.budgets.dto.DeleteBudgetInput
import domain.exceptions.NotFoundException

class DeleteBudget(
    private val budgetRepo: IRepository<Budget>
): UseCase<DeleteBudgetInput, Unit>(){
    override suspend fun process(input: DeleteBudgetInput) {
        budgetRepo.get(input.budgetId) ?: throw NotFoundException.SingleEntity(input.budgetId, "budget")
        budgetRepo.delete(input.budgetId)
    }
}