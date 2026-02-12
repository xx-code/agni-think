package dev.auguste.agni_api.core.entities.enums

enum class PatrimonyType(val value: String) {
    ASSET("Asset"),
    LIABILITY("Liability");

    companion object {
        fun fromString(value: String): PatrimonyType {
            return PatrimonyType.entries.find { it.value.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("Patrimony Type $value not found in enums")
        }
    }
}