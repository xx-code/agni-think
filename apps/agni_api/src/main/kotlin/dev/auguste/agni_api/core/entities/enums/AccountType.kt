package dev.auguste.agni_api.core.entities.enums

enum class AccountType(val value: String) {
    CHECKING("Checking"),
    CREDIT_CARD("CreditCard"),
    SAVING("Saving"),
    BUSINESS("Business"),
    BROKING("Broking");

    companion object {
        fun fromString(value: String): AccountType {
            return AccountType.entries.find { it.value.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("Account Type $value not found in enums")
        }
    }
}