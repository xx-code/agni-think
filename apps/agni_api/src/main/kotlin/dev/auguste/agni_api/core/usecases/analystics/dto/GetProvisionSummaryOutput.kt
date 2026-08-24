package dev.auguste.agni_api.core.usecases.analystics.dto

data class GetProvisionSummaryOutput(
    val activesProvision: Int,
    val initialValue: Double,
    val accountingTotalValue: Double,
    val costByMonth: Double,
    val monthlyPayment: Double
)