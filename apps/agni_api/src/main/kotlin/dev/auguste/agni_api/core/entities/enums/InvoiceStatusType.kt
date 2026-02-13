package dev.auguste.agni_api.core.entities.enums

enum class InvoiceStatusType(val value: String) {
    PENDING("Pending"),
    COMPLETED("Complete");

    companion object {
        fun fromString(value: String): InvoiceStatusType {
            return InvoiceStatusType.entries.find { it.value.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("Invoice Status Type $value not found in enums")
        }
    }
}