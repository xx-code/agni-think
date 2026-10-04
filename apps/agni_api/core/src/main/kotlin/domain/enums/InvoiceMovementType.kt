package domain.enums

import domain.exceptions.ValidationException

enum class InvoiceMovementType(val value: String) {
    CREDIT("Credit"),
    DEBIT("Debit");

    override fun toString(): String {
        return value
    }

    companion object {
        fun fromString(value: String): InvoiceMovementType {
            return entries.find { it.value.equals(value, ignoreCase = true) }
                ?: throw ValidationException.BadType("InvoiceMovementType", value)
        }
    }
}

