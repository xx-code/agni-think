package dev.auguste.agni_api.core.value_objects

import java.sql.Date

data class Scheduler(val date: Date, val repeater: SchedulerRecurrence? = null)

