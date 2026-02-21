package dev.auguste.agni_api.core.entities.enums

enum class IncomeSourceType(val value: String) {
    SALARY("Salary"),
    CONTRACT("Contract"),
    GIG("Gig"),
    PASSIVE("Passive");

    companion object {
        fun fromString(value: String): IncomeSourceType {
            return IncomeSourceType.entries.find { it.value.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("Invoice Source Type $value not found in enums")
        }
    }
}