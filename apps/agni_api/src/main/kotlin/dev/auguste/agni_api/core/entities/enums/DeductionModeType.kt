package dev.auguste.agni_api.core.entities.enums

enum class DeductionModeType(val value: String) {
    FLAT("Flat"),
    RATE("Rate");

    companion object {
        fun fromString(value: String): DeductionModeType {
            return DeductionModeType.entries.find { it.value.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("Deduction Mode Type $value not found in enums")
        }
    }
}