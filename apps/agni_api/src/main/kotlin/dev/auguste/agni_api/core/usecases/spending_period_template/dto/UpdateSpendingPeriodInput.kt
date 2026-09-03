package dev.auguste.agni_api.core.usecases.spending_period_template.dto

import dev.auguste.agni_api.core.adapters.dto.ScheduleRepeaterInput
import java.time.LocalDate
import java.util.UUID

data class UpdateSpendingPeriodTemplateInput(
    val id: UUID,
    val startDate: LocalDate?,
    val isActive: Boolean?,
    val recurrence: ScheduleRepeaterInput? = null,
    val endDate: LocalDate? = null
)