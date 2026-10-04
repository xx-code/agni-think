package domain.enums

import domain.exceptions.ValidationException

enum class PatrimonyType(val value: String) {
    ASSET("Asset"),
    LIABILITY("Liability");

    override fun toString(): String {
        return value
    }

    companion object {
        fun fromString(value: String): PatrimonyType {
            return entries.find { it.value.equals(value, ignoreCase = true) }
                ?: throw ValidationException.BadType("PatrimonyType", value)
        }
    }
}