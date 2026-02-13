package dev.auguste.agni_api.core.entities.enums

enum class ManagementAccountType(val value: String) {
    SELF_DIRECTED("Self_directed"),
    MANAGED("Managed"),
    ROBOT("Robot");

    companion object {
        fun fromString(value: String): ManagementAccountType {
            return ManagementAccountType.entries.find { it.value.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("Managment Account Type $value not found in enums")
        }
    }
}