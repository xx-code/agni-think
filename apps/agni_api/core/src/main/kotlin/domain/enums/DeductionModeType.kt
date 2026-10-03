package domain.enums

import domain.exceptions.ValidationException

enum class DeductionModeType(val value: String) {
    FLAT("Flat"),
    RATE("Rate");

    override fun toString(): String {
        return value
    }

    companion object {
        fun fromString(value: String): DeductionModeType {
            return entries.find { it.value.equals(value, ignoreCase = true) }
                ?: throw ValidationException.BadType("DeductionModeType", value)
        }
    }
}