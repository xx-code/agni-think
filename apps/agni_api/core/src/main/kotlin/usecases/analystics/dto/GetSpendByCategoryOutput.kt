package usecases.analystics.dto

import java.util.UUID

data class GetSpendByCategoryOutput(
    val categoryId: UUID,
    val icon: String,
    val title: String,
    val color: String,
    val spends: List<Double>
)
