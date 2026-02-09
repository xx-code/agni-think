package dev.auguste.agni_api.core.entities.enums

enum class InvoiceStatusType(val value: String) {
    PENDING("Pending"),
    COMPLETED("Complete");

    companion object {
        fun fromString(value: String): InvoiceStatusType {
            return InvoiceStatusType.entries.first { it.value.equals(value, ignoreCase = true) }
        }
    }
}