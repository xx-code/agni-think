package dev.auguste.agni_api.core.entities.enums

enum class DeductionModeType(val value: String) {
    FLAT("Flat"),
    RATE("Rate");

    companion object {
        fun fromString(value: String): DeductionModeType {
            return DeductionModeType.entries.first { it.value.equals(value, ignoreCase = true) }
        }
    }
}