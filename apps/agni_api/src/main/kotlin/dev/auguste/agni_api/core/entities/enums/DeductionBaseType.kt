package dev.auguste.agni_api.core.entities.enums

enum class DeductionBaseType(val value: String) {
    SUBTOTAL("Subtotal"),
    TOTAL("Total");

    companion object {
        fun fromString(value: String): DeductionBaseType {
            return DeductionBaseType.entries.first { it.value.equals(value, ignoreCase = true) }
        }
    }
}