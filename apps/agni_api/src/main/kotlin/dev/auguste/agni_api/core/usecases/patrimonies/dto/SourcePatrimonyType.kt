package dev.auguste.agni_api.core.usecases.patrimonies.dto

enum class SourcePatrimonyType(val value: String) {
    PATRIMONY("Patrimony"),
    FUND("Fund"),
    PROVISION("Provision");

    companion object {
        fun fromString(value: String): SourcePatrimonyType {
            return SourcePatrimonyType.entries.find { it.value.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("source patrimony Type $value not found in enums")
        }
    }
}