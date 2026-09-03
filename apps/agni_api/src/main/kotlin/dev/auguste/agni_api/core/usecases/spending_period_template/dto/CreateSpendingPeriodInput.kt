package dev.auguste.agni_api.core.usecases.spending_period_template.dto

import dev.auguste.agni_api.core.adapters.dto.ScheduleRepeaterInput
import java.time.LocalDate

data class CreateSpendingPeriodTemplateInput(
    val recurrence: ScheduleRepeaterInput,
    val startDate: LocalDate,
    val endDate: LocalDate? = null
)