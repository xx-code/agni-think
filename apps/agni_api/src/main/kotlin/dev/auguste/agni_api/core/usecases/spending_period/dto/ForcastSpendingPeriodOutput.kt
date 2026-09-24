package dev.auguste.agni_api.core.usecases.spending_period.dto

data class ForcastSpendingAchieveItemOutput(
    val description: String,
    val amount: Double,
    val isAchieved: Boolean
)

data class ForcastSpendingPeriodOutput(
    val expectedRemainAmount: Double,
    val currentRemainAmount: Double,
    val totalExpectedIncome: Double,
    val totalExpectedExpense: Double,
    val expectedIncome: Double,
    val expectedFixExpense: Double,
    val expectedVariableExpense: Double,
    val expectedPlanFreezeExpense: Double,
    val expectedBudgetExpense: Double,
    val currentBudgetExpense: Double,
    val expectedSaving: Double,
    val currentSaving: Double,
    val achievedFixExpenses: List<ForcastSpendingAchieveItemOutput>,
    val achievedVariableExpenses: List<ForcastSpendingAchieveItemOutput>,
    val achievedWishedItems: List<ForcastSpendingAchieveItemOutput>
)
