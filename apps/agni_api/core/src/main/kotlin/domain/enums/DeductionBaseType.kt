package domain.enums

import domain.exceptions.ValidationException

enum class DeductionBaseType(val value: String) {
    SUBTOTAL("Subtotal"),
    TOTAL("Total");

    companion object {
        fun fromString(value: String): DeductionBaseType {
            return entries.find { it.value.equals(value, ignoreCase = true) }
                ?: throw ValidationException.BadType("DeductionBaseType", value)
        }
    }
}