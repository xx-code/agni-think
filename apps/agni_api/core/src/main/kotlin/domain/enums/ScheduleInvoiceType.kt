package domain.enums

import domain.exceptions.ValidationException

enum class ScheduleInvoiceType(val value: String) {
    STANDARD("Standard"),
    FREEZE("Freeze"),
    SAVING("Saving");

    override fun toString(): String {
        return value
    }

    companion object {
        fun fromString(value: String): ScheduleInvoiceType {
            return entries.find { it.value.equals(value, ignoreCase = true) }
                ?: throw ValidationException.BadType("ScheduleInvoiceType", value)
        }
    }
}