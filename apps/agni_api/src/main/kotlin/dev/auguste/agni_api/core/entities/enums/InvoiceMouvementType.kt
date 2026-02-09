package dev.auguste.agni_api.core.entities.enums

enum class InvoiceMouvementType(val value: String) {
    CREDIT("Credit"),
    DEBIT("Debit");

    companion object {
        fun fromString(value: String): InvoiceMouvementType {
            return InvoiceMouvementType.entries.first { it.value.equals(value, ignoreCase = true) }
        }
    }
}

