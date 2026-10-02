package usecases.budgets.dto

import java.util.UUID

data class DeleteBudgetInput(
    val budgetId: UUID
)