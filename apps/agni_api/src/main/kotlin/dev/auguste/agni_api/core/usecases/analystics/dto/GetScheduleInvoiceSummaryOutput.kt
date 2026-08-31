package dev.auguste.agni_api.core.usecases.analystics.dto

data class GetScheduleInvoiceSummaryOutput(
    val totalPlan: Int,
    val totalActives: Int,
    val totalPause: Int,
    val totalAmountActive: Double
)
