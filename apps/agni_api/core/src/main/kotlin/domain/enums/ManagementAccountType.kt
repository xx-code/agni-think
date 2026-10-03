package domain.enums

import domain.exceptions.ValidationException

enum class ManagementAccountType(val value: String) {
    SELF_DIRECTED("Self_directed"),
    MANAGED("Managed"),
    ROBOT("Robot");

    override fun toString(): String {
        return value
    }

    companion object {
        fun fromString(value: String): ManagementAccountType {
            return entries.find { it.value.equals(value, ignoreCase = true) }
                ?: throw ValidationException.BadType("ManagementAccountType", value)
        }
    }
}