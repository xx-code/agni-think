package dev.auguste.agni_api.core.value_objects

import dev.auguste.agni_api.core.entities.enums.PeriodType


data class SchedulerRecurrence(val period: PeriodType, val interval: Int)
