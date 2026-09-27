package dev.auguste.agni_api.core.entities.enums

enum class InvoiceModuleLinkerType(val value: String) {
    FUND("Fund"),
    SCHEDULE_INVOICE("ScheduleInvoice"),
    PROVISION("Provision"),
    TRANSFER("Transfer");

    companion object {
        fun fromString(value: String): InvoiceModuleLinkerType {
            return entries.find { it.value.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("Invoice ModuleLinker Type $value not found in enums")
        }
    }
}