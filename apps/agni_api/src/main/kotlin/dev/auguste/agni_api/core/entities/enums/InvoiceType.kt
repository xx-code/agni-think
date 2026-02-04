package dev.auguste.agni_api.core.entities.enums

enum class InvoiceType(val value: String) {
    INCOME("Income"),
    FIXEDCOST("FixedCost"),
    VARIABLECOST("VariableCost"),
    OTHER("Other")
}