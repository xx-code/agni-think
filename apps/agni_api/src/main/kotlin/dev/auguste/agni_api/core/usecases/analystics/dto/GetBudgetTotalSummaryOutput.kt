package dev.auguste.agni_api.core.usecases.analystics.dto

data class GetBudgetTotalSummaryOutput(
    val totalBudget: Long,
    val totalSpend: Long
)