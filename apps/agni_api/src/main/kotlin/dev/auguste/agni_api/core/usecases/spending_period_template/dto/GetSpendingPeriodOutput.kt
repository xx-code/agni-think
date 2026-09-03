package dev.auguste.agni_api.core.usecases.spending_period_template.dto

import dev.auguste.agni_api.core.adapters.dto.ScheduleRepeaterOutput
import java.time.LocalDate
import java.util.UUID

data class GetSpendingPeriodTemplateBudgetOutput(
    val id: UUID,
    val title: String
)

data class GetSpendingPeriodTemplateOutput(
    val id: UUID,
    val recurrence: ScheduleRepeaterOutput,
    val isActive: Boolean,
    val budgets: List<GetSpendingPeriodTemplateBudgetOutput>,
    val startDate: LocalDate,
    val endDate: LocalDate?
)