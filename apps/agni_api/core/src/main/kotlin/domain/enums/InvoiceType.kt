package domain.enums

import domain.exceptions.ValidationException

enum class InvoiceType(val value: String) {
    INCOME("Income"),
    FIXED_COST("FixedCost"),
    VARIABLE_COST("VariableCost"),
    OTHER("Other");

    override fun toString(): String {
        return value
    }

    companion object {
        fun fromString(value: String): InvoiceType {
            return entries.find { it.value.equals(value, ignoreCase = true) }
                ?: throw ValidationException.BadType("InvoiceType", value)
        }
    }
}