package dev.auguste.agni_api.core.entities.enums

enum class ScheduleInvoiceModuleLinkerType(val value: String) {
    FUND("Fund"),
    PROVISION("Provision"),
    INCOME_SOURCE("IncomeSource");

    companion object {
        fun fromString(value: String): ScheduleInvoiceModuleLinkerType {
            return entries.find { it.value.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("Schedule Invoice ModuleLinker Type $value not found in enums")
        }
    }
}