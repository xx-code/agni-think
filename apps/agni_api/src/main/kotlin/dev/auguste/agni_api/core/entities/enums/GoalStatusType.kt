package dev.auguste.agni_api.core.entities.enums

enum class GoalStatusType {
    ACTIVE,
    COMPLETED,
    PAUSED;

    companion object {
        fun fromInt(value: Int): GoalStatusType {
            if (value !in 0..2)
                throw IllegalArgumentException("Invalid GoalStatus value $value")

            return GoalStatusType.entries[value]
        }
    }
}