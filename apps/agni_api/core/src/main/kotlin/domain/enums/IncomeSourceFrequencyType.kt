package domain.enums

import domain.exceptions.ValidationException

enum class IncomeSourceFrequencyType(val value: String) {
    WEEKLY("Weekly"),
    BIWEEKLY("Biweekly"),
    MONTHLY("Monthly"),
    YEARLY("Yearly"),
    IRRELEVANTLY("Irrelevant");

    companion object {
        fun fromString(value: String): IncomeSourceFrequencyType {
            return entries.find { it.value.equals(value, ignoreCase = true) }
                ?: throw ValidationException.BadType("IncomeSourceFrequencyType", value)
        }
    }
}