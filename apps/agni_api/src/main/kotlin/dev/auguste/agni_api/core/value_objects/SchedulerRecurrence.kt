package dev.auguste.agni_api.core.value_objects

import dev.auguste.agni_api.core.entities.enums.PeriodType
import java.time.LocalDate
import java.time.temporal.ChronoUnit


data class SchedulerRecurrence(val period: PeriodType, val interval: Int) {
    fun toMap(): Map<String, Any?> {
        return mapOf("period" to period.value, "interval" to interval)
    }

    fun computeOccurrences(startDate: LocalDate, endDate: LocalDate): Int {
        if (startDate.isAfter(endDate)) return 0

        var count = 0
        var current = startDate

        while (!current.isAfter(endDate)) {
            count++
            current = when (period) {
                PeriodType.YEAR -> current.plusYears(interval.toLong())
                PeriodType.MONTH -> current.plusMonths(interval.toLong())
                PeriodType.WEEK -> current.plusWeeks(interval.toLong())
                PeriodType.DAY -> current.plusDays(interval.toLong())
            }
        }

        return count
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
