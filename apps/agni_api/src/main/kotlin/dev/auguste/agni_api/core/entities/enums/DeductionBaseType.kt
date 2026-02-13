package dev.auguste.agni_api.core.entities.enums

enum class DeductionBaseType(val value: String) {
    SUBTOTAL("Subtotal"),
    TOTAL("Total");

    companion object {
        fun fromString(value: String): DeductionBaseType {
            return DeductionBaseType.entries.find { it.value.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("Deduction Base Type $value not found in enums")
        }
    }
}