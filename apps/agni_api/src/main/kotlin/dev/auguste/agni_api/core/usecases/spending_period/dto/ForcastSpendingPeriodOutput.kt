package dev.auguste.agni_api.core.usecases.spending_period.dto

data class ForcastSpendingAchieveItemOutput(
    val description: String,
    val amount: Double,
    val validAmount: Double,
    val isAchieved: Boolean
)

data class ForcastSpendingPeriodOutput(
    val expectedRemainAmount: Double,
    val currentRemainAmount: Double,
    val totalExpectedIncome: Double,
    val totalExpectedExpense: Double,
    val expectedFixExpense: Double,
    val expectedVariableExpense: Double,
    val expectedBudgetExpense: Double,
    val currentIncome: Double,
    val expectedSaving: Double,
    val currentSaving: Double,
    val currentBudgetExpense: Double,
    val incomeItems: List<ForcastSpendingAchieveItemOutput>,
    val fixExpenseItems: List<ForcastSpendingAchieveItemOutput>,
    val variableExpenseItems: List<ForcastSpendingAchieveItemOutput>,
    val achievedWishedItems: List<ForcastSpendingAchieveItemOutput>
)
