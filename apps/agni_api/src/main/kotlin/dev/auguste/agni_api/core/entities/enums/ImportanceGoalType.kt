package dev.auguste.agni_api.core.entities.enums

enum class ImportanceGoalType(val type: Int) {
    INSIGNIFICANT(1),
    NORMAL(2),
    IMPORTANT(3),
    URGENT(4);

    companion object {
        fun fromInt(value: Int): ImportanceGoalType {
            if (value == 0 || value > 4)
                throw IllegalArgumentException("Invalid importance goal type: $value")

            return ImportanceGoalType.entries[value - 1]
        }
    }
}