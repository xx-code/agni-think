package dev.auguste.agni_api.core.usecases.analystics.dto

data class GetPatrimonySummaryOutput(
    val networth: Double,
    val passNetworth: Double,
    val totalAsset: Double,
    val passTotalAsset: Double,
    val totalLiability: Double,
    val passTotalLiability: Double,
    val monthlyEvolutionPerc: Double,
)