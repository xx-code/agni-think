package domain.enums

import domain.exceptions.ValidationException

enum class ImportanceGoalType(val type: Int) {
    INSIGNIFICANT(1),
    NORMAL(2),
    IMPORTANT(3),
    URGENT(4);

    companion object {
        fun fromInt(value: Int): ImportanceGoalType {
            if (value == 0 || value > 4)
                throw ValidationException.BadType("ImportanceGoalType", value.toString())

            return entries[value - 1]
        }
    }
}