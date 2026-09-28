package dev.auguste.agni_api.core.entities.enums

enum class FundType(val value: String) {
    EMERGENCY("Emergency"),
    AMORTIZATION("Amortization"),
    SINKING_FUND("SinkingFund"),
    PROJECT_TARGET("ProjectTarget"),
    OPPORTUNITY("Opportunity"),
    SAVINGS_GENERAL("SavingsGeneral");

    companion object {
        fun fromString(value: String): FundType {
            return FundType.entries.find { it.value.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("Fund Type $value not found in enums")
        }
    }
}