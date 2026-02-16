package dev.auguste.agni_api.core.entities.enums

enum class IncomeSourceFrequencyType(val value: String) {
    WEEKLY("Weekly"),
    BIWEEKLY("Biweekly"),
    MONTHLY("Monthly"),
    YEARLY("Yearly"),
    IRRELEVANTLY("Irrelevant");

    companion object {
        fun fromString(value: String): IncomeSourceFrequencyType {
            return IncomeSourceFrequencyType.entries.find { it.value.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("Invoice Source Frequency Type $value not found in enums")        }
    }
}