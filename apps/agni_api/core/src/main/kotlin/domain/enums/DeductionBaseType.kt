package domain.enums

import domain.exceptions.ValidationException

enum class DeductionBaseType(val value: String) {
    SUBTOTAL("Subtotal"),
    TOTAL("Total");

    override fun toString(): String {
        return value
    }

    companion object {
        fun fromString(value: String): DeductionBaseType {
            return entries.find { it.value.equals(value, ignoreCase = true) }
                ?: throw ValidationException.BadType("DeductionBaseType", value)
        }
    }
}