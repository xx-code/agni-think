package dev.auguste.agni_api.core.value_objects

import dev.auguste.agni_api.core.entities.enums.PeriodType
import java.time.LocalDateTime


data class Scheduler(val date: LocalDateTime, val repeater: SchedulerRecurrence? = null) {
    fun isDueDate(): Boolean = date.isBefore(LocalDateTime.now())

    fun upgradeDate(): LocalDateTime? {
        if (repeater == null)
            return null

        val now = LocalDateTime.now()
        var next = date

        do {
            next = when(repeater.period) {
                PeriodType.YEAR -> date.plusDays(repeater.interval.toLong())
                PeriodType.MONTH -> date.plusMonths(repeater.interval.toLong())
                PeriodType.WEEK -> date.plusWeeks(repeater.interval.toLong())
                PeriodType.DAY -> date.plusDays(repeater.interval.toLong())
            }
        } while (next.isBefore(now))

        return next
    }
}

