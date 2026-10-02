package domain.enums

import domain.exceptions.ValidationException

enum class PeriodType(val value: String) {
    YEAR("Year"),
    MONTH("Month"),
    WEEK("Week"),
    DAY("Day");

    companion object {
        fun fromString(value: String): PeriodType {
            return entries.find { it.value.equals(value, ignoreCase = true) }
                ?: throw ValidationException.BadType("PeriodType", value)
        }
    }
}