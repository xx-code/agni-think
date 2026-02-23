package dev.auguste.agni_api.core.value_objects

import dev.auguste.agni_api.core.entities.enums.PeriodType
import java.time.LocalDate
import java.time.temporal.ChronoUnit


data class SchedulerRecurrence(val period: PeriodType, val interval: Int) {
    fun toMap(): Map<String, Any?> {
        return mapOf("period" to period, "interval" to interval)
    }

    fun computeOccurrences(startDate: LocalDate, endDate: LocalDate): Int {
        val occurrences = when (period) {
            PeriodType.YEAR -> ChronoUnit.YEARS.between(startDate, endDate)
            PeriodType.MONTH -> ChronoUnit.MONTHS.between(startDate, endDate)
            PeriodType.WEEK -> ChronoUnit.WEEKS.between(startDate, endDate)
            PeriodType.DAY -> ChronoUnit.DAYS.between(startDate, endDate)
        }
        return (occurrences / interval).toInt()
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
