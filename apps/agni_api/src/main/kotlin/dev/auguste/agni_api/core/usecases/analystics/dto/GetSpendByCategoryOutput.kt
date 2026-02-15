package dev.auguste.agni_api.core.usecases.analystics.dto

import java.util.UUID

data class GetSpendByCategoryOutput(
    val categoryId: UUID,
    val spends: List<Double>
)
