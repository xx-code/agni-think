package domain.enums

import domain.exceptions.ValidationException

enum class ProvisionType(val value: String) {
    DEPRECIATE("Depreciate"),
    DEPRECIATE_LOAN("DepreciateLoan");

    override fun toString(): String {
        return value
    }

    companion object {
        fun fromString(value: String): ProvisionType {
            return entries.find { it.value.equals(value, ignoreCase = true) }
                ?: throw ValidationException.BadType("ProvisionType", value)
        }
    }
}