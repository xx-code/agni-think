package dev.auguste.agni_api.core.entities.enums

enum class InvoiceType(val value: String) {
    INCOME("Income"),
    FIXEDCOST("FixedCost"),
    VARIABLECOST("VariableCost"),
    OTHER("Other");

    companion object {
        fun fromString(value: String): InvoiceType {
            return InvoiceType.entries.first { it.value.equals(value, ignoreCase = true) }
        }
    }
}