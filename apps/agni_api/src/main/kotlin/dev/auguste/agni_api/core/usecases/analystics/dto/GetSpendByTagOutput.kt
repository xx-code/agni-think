package dev.auguste.agni_api.core.usecases.analystics.dto

import java.util.UUID

data class GetSpendByTagOutput(
    val tagId: UUID,
    val spends: List<Double>
)
