package dev.auguste.agni_api.core.entities.enums

enum class ContributionAccountType(val value: String) {
    REGISTERED("Registered"),
    UNREGISTERED("Unregistered");

    companion object {
        fun fromString(value: String): ContributionAccountType {
            return ContributionAccountType.entries.find { it.value.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("Contribution Account Type $value not found in enums")
        }
    }
}