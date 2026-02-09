package dev.auguste.agni_api.core.entities.enums

enum class PeriodType(val value: String) {
    YEAR("Year"),
    MONTH("Month"),
    WEEK("Week"),
    DAY("Day");

    companion object {
        fun fromString(value: String): PeriodType {
            return PeriodType.entries.first { it.value.equals(value, ignoreCase = true) }
        }
    }
}