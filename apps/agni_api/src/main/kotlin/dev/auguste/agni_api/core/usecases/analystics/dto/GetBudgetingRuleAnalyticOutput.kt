package dev.auguste.agni_api.core.usecases.analystics.dto

data class GetBudgetingRuleAnalyticOutput(
    val ratioSaving: Double,
    val ratioFixCost: Double,
    val ratioVariableCost: Double,
    val savingAmount: Double,
    val fixCost: Double,
    val variableCost: Double,
    val income: Double
)