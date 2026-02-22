package dev.auguste.agni_api.core.entities.utils

import dev.auguste.agni_api.core.entities.enums.PeriodType
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
                PeriodType.MONTH -> now.minusMonths(interval.toLong())
                    .with(TemporalAdjusters.firstDayOfMonth())
                PeriodType.WEEK -> now.minusWeeks(interval.toLong())
                    .with(java.time.DayOfWeek.MONDAY)
                PeriodType.YEAR -> now.minusYears(interval.toLong())
                    .with(TemporalAdjusters.firstDayOfYear())
                PeriodType.DAY -> now.minusDays(interval.toLong())
            }.with(LocalTime.MIN) // Force 00:00:00

            // La fin est soit le "maintenant" relatif à l'intervalle,
            // soit la fin de la journée en cours.
            val endDate = if (interval == 0) {
                now.with(LocalTime.MAX)
            } else {
                // Si on regarde le mois passé (interval 1), on veut la fin de ce mois là
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