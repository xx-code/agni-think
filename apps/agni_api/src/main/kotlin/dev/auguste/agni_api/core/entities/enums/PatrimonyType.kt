package dev.auguste.agni_api.core.entities.enums

enum class PatrimonyType(val value: String) {
    ASSET("Asset"),
    LIABILITY("Liability");

    companion object {
        fun fromString(value: String): PatrimonyType {
            return PatrimonyType.entries.first { it.value.equals(value, ignoreCase = true) }
        }
    }
}