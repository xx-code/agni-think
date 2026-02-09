package dev.auguste.agni_api.core.entities.enums

enum class ManagementAccountType(val value: String) {
    SELF_DIRECTED("Self_directed"),
    MANAGED("Managed"),
    ROBOT("Robot");

    companion object {
        fun fromString(value: String): ManagementAccountType {
            return ManagementAccountType.entries.first { it.value.equals(value, ignoreCase = true) }
        }
    }
}