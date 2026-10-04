package domain.enums

import domain.exceptions.ValidationException

enum class AccountType(val value: String) {
    CHECKING("Checking"),
    CREDIT_CARD("CreditCard"),
    SAVING("Saving"),
    BUSINESS("Business"),
    BROKING("Broking");

    override fun toString(): String {
        return value
    }

    companion object {
        fun fromString(value: String): AccountType {
            return entries.find { it.value.equals(value, ignoreCase = true) }
                ?: throw ValidationException.BadType("AccountType", value)
        }
    }
}