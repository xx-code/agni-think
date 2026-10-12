package domain.utils

import domain.enums.PeriodType
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.TemporalAdjusters

data class LocalDateTimeRange(
    val start: LocalDateTime,
    val end: LocalDateTime
) {
    companion object {
        fun fromPeriod(period: PeriodType, interval: Int): LocalDateTimeRange {
            val now = LocalDateTime.now()

            val startDate = when (period) {
                PeriodType.MONTH -> now.with(TemporalAdjusters.firstDayOfMonth())
                PeriodType.WEEK -> now.with(DayOfWeek.MONDAY)
                PeriodType.YEAR -> now.with(TemporalAdjusters.firstDayOfYear())
                PeriodType.DAY -> now
            }.with(LocalTime.MIN) // Force 00:00:00

            val endDate = if (interval == 0) {
                now.with(LocalTime.MAX)
            } else {
                when (period) {
                    PeriodType.MONTH -> startDate.with(TemporalAdjusters.lastDayOfMonth())
                    PeriodType.WEEK -> startDate.plusDays(6)
                    PeriodType.YEAR -> startDate.with(TemporalAdjusters.lastDayOfYear())
                    PeriodType.DAY -> startDate
                }.with(LocalTime.MAX)
            }

            return LocalDateTimeRange(startDate, endDate)
        }
    }
}