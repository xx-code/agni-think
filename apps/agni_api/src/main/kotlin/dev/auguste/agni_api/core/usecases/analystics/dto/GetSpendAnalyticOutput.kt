package dev.auguste.agni_api.core.usecases.analystics.dto

import java.util.UUID

data class SpendByTagOutput(
    val tagId: UUID,
    val spend: Double
)

data class SpendByCategoryOutput(
    val categoryId: UUID,
    val spend: Double,
    val spendByTags: Set<SpendByTagOutput>
)

data class GetSpendAnalyticOutput(
    val totalSpend: List<Double>,
    val spendByCategories: List<Set<SpendByCategoryOutput>>,
)
