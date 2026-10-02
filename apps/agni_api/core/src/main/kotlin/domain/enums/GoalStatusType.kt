package domain.enums

import domain.exceptions.ValidationException

enum class GoalStatusType {
    ACTIVE,
    COMPLETED,
    PAUSED;

    companion object {
        fun fromInt(value: Int): GoalStatusType {
            if (value !in 0..2)
                throw ValidationException.BadType("GoalStatusType", value.toString())

            return entries[value]
        }
    }
}