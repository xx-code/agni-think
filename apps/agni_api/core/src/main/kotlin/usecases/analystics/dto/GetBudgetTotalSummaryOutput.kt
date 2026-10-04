package usecases.analystics.dto

data class GetBudgetTotalSummaryOutput(
    val totalBudget: Long,
    val totalSpend: Long
)