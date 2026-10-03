package domain.enums

import domain.exceptions.ValidationException

enum class SpendingPeriodStateType(val value: String) {
    DRAFT("Draft"),
    TO_REVIEW("ToReview"),
    IN_PROGRESS("InProgress"),
    COMPLETE("Complete");

    override fun toString(): String {
        return value
    }

    companion object {
        fun fromString(value: String): SpendingPeriodStateType {
            return entries.find { it.value.equals(value, ignoreCase = true) }
                ?: throw ValidationException.BadType("SpendingPeriodStateType", value)
        }
    }
}