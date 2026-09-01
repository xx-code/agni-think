package dev.auguste.agni_api.core.usecases.analystics.dto

data class WantItemOutput(
    val description: String,
    val amount: Double
)

data class ForcastSpendingOutput(
    val remainAmount: Double,
    val totalExpectedIncome: Double,
    val totalExpectedExpense: Double,
    val expectedIncome: Double,
    val expectedFixExpense: Double,
    val expectedVariableExpense: Double,
    val expectedPlanFreezeExpense: Double,
    val expectedBudgetExpense: Double,
    val expectedSaving: Double,
    val itemsApproved: List<WantItemOutput>,
    val itemsRejected: List<WantItemOutput>
)