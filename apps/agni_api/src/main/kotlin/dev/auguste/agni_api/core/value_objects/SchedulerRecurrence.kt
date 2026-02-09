package dev.auguste.agni_api.core.value_objects

import dev.auguste.agni_api.core.entities.enums.PeriodType


data class SchedulerRecurrence(val period: PeriodType, val interval: Int) {
    fun toMap(): Map<String, Any?> {
        return mapOf("period" to period, "interval" to interval)
    }

    companion object {
        fun fromMap(map: Map<String, Any>?): SchedulerRecurrence {
            if (map == null)
                return SchedulerRecurrence(PeriodType.DAY, 1)

            if (!map.containsKey("period") || !map.containsKey("interval")) {
                return SchedulerRecurrence(PeriodType.DAY, 1)
            }

            return SchedulerRecurrence(PeriodType.fromString(map.getValue("period") as String), map.getValue("interval") as Int)
        }
    }
}
