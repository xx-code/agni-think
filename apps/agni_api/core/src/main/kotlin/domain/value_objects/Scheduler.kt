package domain.value_objects

import domain.enums.PeriodType
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset


data class Scheduler(var date: LocalDateTime, val repeater: SchedulerRecurrence? = null) {
    fun isDueDate(): Boolean {
        val now = LocalDateTime.now()
        return !date.isAfter(now)
    }

    fun upgradeDate(toDate: LocalDateTime = LocalDateTime.now()): LocalDateTime {
        if (repeater == null)
            return date

        require(repeater.interval > 0) {
            "Repeater interval must be strictly positive, got ${repeater.interval}"
        }

        var next = date

        while (next.isBefore(toDate)) {
            next = when(repeater.period) {
                _root_ide_package_.domain.enums.PeriodType.YEAR -> next.plusYears(repeater.interval.toLong())
                _root_ide_package_.domain.enums.PeriodType.MONTH -> next.plusMonths(repeater.interval.toLong())
                _root_ide_package_.domain.enums.PeriodType.WEEK -> next.plusWeeks(repeater.interval.toLong())
                _root_ide_package_.domain.enums.PeriodType.DAY -> next.plusDays(repeater.interval.toLong())
            }
        }

        return next
    }

    fun downgradeDate(): LocalDateTime? {
        if (repeater == null)
            return null

        val now = LocalDateTime.now()
        var next = date

        while (next.isAfter(now)) {
            next = when(repeater.period) {
                _root_ide_package_.domain.enums.PeriodType.YEAR -> next.minusYears(repeater.interval.toLong())
                _root_ide_package_.domain.enums.PeriodType.MONTH -> next.minusMonths(repeater.interval.toLong())
                _root_ide_package_.domain.enums.PeriodType.WEEK -> next.minusWeeks(repeater.interval.toLong())
                _root_ide_package_.domain.enums.PeriodType.DAY -> next.minusDays(repeater.interval.toLong())
            }
        }

        return next
    }

    fun toMap(): Map<String, Any?> {
        if (repeater == null)
            return mapOf("due_date" to date.atOffset(ZoneOffset.UTC))

        return mapOf("due_date" to date.atOffset(ZoneOffset.UTC), "repeater" to repeater.toMap())
    }

    companion object {
        fun fromMap(map: Map<String, Any>?): Scheduler {
            if (map == null)
                return Scheduler(LocalDateTime.now(), null)

            if (map.containsKey("repeater"))
                return Scheduler(date = OffsetDateTime.parse(map.getValue("due_date") as String).toLocalDateTime(), repeater = SchedulerRecurrence.fromMap( map.getValue("repeater") as Map<String, Any>)  )

            return Scheduler(date = OffsetDateTime.parse(map.getValue("due_date") as String).toLocalDateTime())
        }
    }
}

