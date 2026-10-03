package domain.enums

import domain.exceptions.ValidationException

enum class ContributionAccountType(val value: String) {
    REGISTERED("Registered"),
    UNREGISTERED("Unregistered");

    override fun toString(): String {
        return value
    }
    companion object {
        fun fromString(value: String): ContributionAccountType {
            return entries.find { it.value.equals(value, ignoreCase = true) }
                ?: throw ValidationException.BadType("ContributionAccountType", value)
        }
    }
}