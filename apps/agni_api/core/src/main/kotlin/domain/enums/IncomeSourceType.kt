package domain.enums

import domain.exceptions.ValidationException

enum class IncomeSourceType(val value: String) {
    SALARY("Salary"),
    CONTRACT("Contract"),
    GIG("Gig"),
    PASSIVE("Passive");

    override fun toString(): String {
        return value
    }

    companion object {
        fun fromString(value: String): IncomeSourceType {
            return entries.find { it.value.equals(value, ignoreCase = true) }
                ?: throw ValidationException.BadType("IncomeSourceType", value)
        }
    }
}