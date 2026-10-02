package domain.enums

import domain.exceptions.ValidationException

enum class FundType(val value: String) {
    EMERGENCY("Emergency"),
    AMORTIZATION("Amortization"),
    SINKING_FUND("SinkingFund"),
    PROJECT_TARGET("ProjectTarget"),
    OPPORTUNITY("Opportunity"),
    SAVINGS_GENERAL("SavingsGeneral");

    companion object {
        fun fromString(value: String): FundType {
            return entries.find { it.value.equals(value, ignoreCase = true) }
                ?: throw ValidationException.BadType("FundType", value)
        }
    }
}