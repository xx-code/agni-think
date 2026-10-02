package domain.value_objects

import domain.enums.PeriodType
import java.time.LocalDate
import java.time.temporal.ChronoUnit


data class SchedulerRecurrence(val period: domain.enums.PeriodType, val interval: Int) {
    fun toMap(): Map<String, Any?> {
        return mapOf("period" to period.value, "interval" to interval)
    }

    fun computeOccurrences(startDate: LocalDate, endDate: LocalDate): Int {
        if (startDate.isAfter(endDate) || interval <= 0) return 0

        var count = 0
        var current = startDate

        while (!current.isAfter(endDate)) {
            count++
            current = when (period) {
                _root_ide_package_.domain.enums.PeriodType.YEAR -> current.plusYears(interval.toLong())
                _root_ide_package_.domain.enums.PeriodType.MONTH -> current.plusMonths(interval.toLong())
                _root_ide_package_.domain.enums.PeriodType.WEEK -> current.plusWeeks(interval.toLong())
                _root_ide_package_.domain.enums.PeriodType.DAY -> current.plusDays(interval.toLong())
            }
        }

        return count
    }

    companion object {
        fun fromMap(map: Map<String, Any>?): SchedulerRecurrence {
            if (map == null)
                return SchedulerRecurrence(_root_ide_package_.domain.enums.PeriodType.DAY, 1)

            if (!map.containsKey("period") || !map.containsKey("interval")) {
                return SchedulerRecurrence(_root_ide_package_.domain.enums.PeriodType.DAY, 1)
            }

            return SchedulerRecurrence(_root_ide_package_.domain.enums.PeriodType.fromString(map.getValue("period") as String), map.getValue("interval") as Int)
        }
    }
}
