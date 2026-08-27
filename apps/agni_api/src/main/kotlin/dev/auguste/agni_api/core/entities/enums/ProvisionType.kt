package dev.auguste.agni_api.core.entities.enums

enum class ProvisionType(val value: String) {
    DEPRECIATE("Depreciate"),
    DEPRECIATE_LOAN("DepreciateLoan");

    companion object {
        fun fromString(value: String): ProvisionType {
            return ProvisionType.entries.find { it.value.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("Provision Type $value not found in enums")
        }
    }
}