package dev.auguste.agni_api.core.usecases.budgets.dto

import java.util.UUID

data class DeleteBudgetInput(
    val budgetId: UUID
)