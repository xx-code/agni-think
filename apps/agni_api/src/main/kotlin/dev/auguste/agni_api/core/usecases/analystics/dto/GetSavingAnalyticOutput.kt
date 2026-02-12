package dev.auguste.agni_api.core.usecases.analystics.dto

data class GetSavingAnalyticOutput (
    val savings: List<Double>,
    val investments: List<Double>,
    val savingRates: List<Double>,
    val investmentRate: List<Double>,
)