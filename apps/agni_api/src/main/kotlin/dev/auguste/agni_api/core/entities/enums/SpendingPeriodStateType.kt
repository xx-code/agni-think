package dev.auguste.agni_api.core.entities.enums

enum class SpendingPeriodStateType(val value: String) {
    PENDING("Pending"),
    CANCEL("Cancel"),
    COMPLETE("Complete");

    companion object {
        fun fromString(value: String): SpendingPeriodStateType {
            return SpendingPeriodStateType.entries.find { it.value.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("Spending Period State Type $value not found in enums")
        }
    }
}