package domain.enums

import domain.exceptions.ValidationException

enum class DepreciationType(val value: String) {
    DECLINING_BALANCE("DecliningBalance"),
    STRAIGHT_LINE("StraightLine"),
    FIX("fix"),
    FIX_PERCENTAGE("FixPercentage");

    companion object {
        fun fromString(value: String): DepreciationType {
            return entries.find { it.value.equals(value, ignoreCase = true) }
                ?: throw ValidationException.BadType("DepreciationType", value)
        }
    }
}