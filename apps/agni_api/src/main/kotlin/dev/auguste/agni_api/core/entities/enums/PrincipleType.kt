package dev.auguste.agni_api.core.entities.enums

enum class PrincipleType(val value: String) {
    EMERGENCY_FUND("EmergencyFund"),
    UPGRADE("Upgrade"),
    INVESTMENT("Investment");

    companion object {
        fun fromString(value: String): PrincipleType {
            return PrincipleType.entries.find { it.value.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("Principe type $value not found in enums")
        }
    }
}