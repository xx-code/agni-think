package domain.enums

import domain.exceptions.ValidationException

enum class InvoiceStatusType(val value: String) {
    PENDING("Pending"),
    COMPLETED("Complete");

    companion object {
        fun fromString(value: String): InvoiceStatusType {
            return entries.find { it.value.equals(value, ignoreCase = true) }
                ?: throw ValidationException.BadType("InvoiceStatusType", value)
        }
    }
}