package domain.enums

import domain.exceptions.ValidationException

enum class PrincipleType(val value: String) {
    EMERGENCY_FUND("EmergencyFund"),
    UPGRADE("Upgrade"),
    INVESTMENT("Investment");

    companion object {
        fun fromString(value: String): PrincipleType {
            return entries.find { it.value.equals(value, ignoreCase = true) }
                ?: throw ValidationException.BadType("PrincipleType", value)
        }
    }
}