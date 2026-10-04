package domain.enums

import domain.exceptions.ValidationException

enum class PrincipleType(val value: String) {
    EMERGENCY_FUND("EmergencyFund"),
    UPGRADE("Upgrade"),
    INVESTMENT("Investment");

    override fun toString(): String {
        return value
    }

    companion object {
        fun fromString(value: String): PrincipleType {
            return entries.find { it.value.equals(value, ignoreCase = true) }
                ?: throw ValidationException.BadType("PrincipleType", value)
        }
    }
}