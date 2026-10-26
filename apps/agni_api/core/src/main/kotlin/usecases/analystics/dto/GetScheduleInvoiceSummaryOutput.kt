package usecases.analystics.dto

data class GetScheduleInvoiceSummaryOutput(
    val totalPlan: Int,
    val totalActives: Int,
    val totalPause: Int,
    val totalAmountSchedule: Double,
    val totalAmountContribution: Double,
    val totalAmountActive: Double
)
