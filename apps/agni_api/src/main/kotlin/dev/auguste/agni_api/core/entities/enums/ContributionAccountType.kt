package dev.auguste.agni_api.core.entities.enums

enum class ContributionAccountType(val value: String) {
    REGISTERED("Registered"),
    UNREGISTERED("Unregistered");

    companion object {
        fun fromString(value: String): ContributionAccountType {
            return ContributionAccountType.entries.first { it.value.equals(value, ignoreCase = true) }
        }
    }
}