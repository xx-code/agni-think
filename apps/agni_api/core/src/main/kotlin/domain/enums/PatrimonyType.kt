package domain.enums

import domain.exceptions.ValidationException

enum class PatrimonyType(val value: String) {
    ASSET("Asset"),
    LIABILITY("Liability");

    companion object {
        fun fromString(value: String): PatrimonyType {
            return entries.find { it.value.equals(value, ignoreCase = true) }
                ?: throw ValidationException.BadType("PatrimonyType", value)
        }
    }
}