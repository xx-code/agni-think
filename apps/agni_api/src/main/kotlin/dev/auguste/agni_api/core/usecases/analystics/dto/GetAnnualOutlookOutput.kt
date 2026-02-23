package dev.auguste.agni_api.core.usecases.analystics.dto

import java.util.UUID

data class SpendByCategoryOutlook(
    val categoryId: UUID,
    val spend: Double
)

data class GetAnnualOutlookOutput(
    val incomeOutlook: Double,
    val spendOutlook: Double,
    val budgetOutlook: Double,
    val savingMargin: Double,
    val currentIncomeOutlook: Double,
    val currentSpendOutlook: Double,
    val currentBudgetOutlook: Double,
    val currentSaving: Double,
    val spendByCategoriesOutlook: List<SpendByCategoryOutlook>,
    val currentSpendByCategoryOutlook: List<SpendByCategoryOutlook>
)
