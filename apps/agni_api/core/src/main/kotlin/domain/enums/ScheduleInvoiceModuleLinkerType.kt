package domain.enums

import domain.exceptions.ValidationException

enum class ScheduleInvoiceModuleLinkerType(val value: String) {
    FUND("Fund"),
    PROVISION("Provision"),
    INCOME_SOURCE("IncomeSource");

    override fun toString(): String {
        return value
    }

    companion object {
        fun fromString(value: String): ScheduleInvoiceModuleLinkerType {
            return entries.find { it.value.equals(value, ignoreCase = true) }
                ?: throw ValidationException.BadType("ScheduleInvoiceModuleLinkerType", value)
        }
    }
}