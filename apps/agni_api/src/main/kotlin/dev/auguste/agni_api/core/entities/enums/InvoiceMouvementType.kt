package dev.auguste.agni_api.core.entities.enums

enum class InvoiceMouvementType(val value: String) {
    CREDIT("Credit"),
    DEBIT("Debit");

    companion object {
        fun fromString(value: String): InvoiceMouvementType {
            return InvoiceMouvementType.entries.find { it.value.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("Invoice Mouvement Type $value not found in enums")
        }
    }
}

