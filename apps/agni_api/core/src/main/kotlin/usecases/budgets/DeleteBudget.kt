package usecases.budgets

import adapters.repositories.IRepository
import domain.entities.Budget
import usecases.budgets.dto.DeleteBudgetInput
import usecases.interfaces.IUseCase
import domain.exceptions.NotFoundException

class DeleteBudget(
    private val budgetRepo: IRepository<Budget>
): IUseCase<DeleteBudgetInput, Unit>{
    override fun execAsync(input: DeleteBudgetInput) {
        budgetRepo.get(input.budgetId) ?: throw NotFoundException.SingleEntity(input.budgetId, "budget")
        budgetRepo.delete(input.budgetId)
    }
}