package domain.enums

import domain.exceptions.ValidationException

enum class FinancePolicyRiskLevelType {
    LOW,
    MEDIUM,
    HIGH;

    companion object {
        fun fromInt(value: Int): FinancePolicyRiskLevelType {
            if (value !in 0..3)
                throw ValidationException.BadType("FinancePolicyRiskLevelType", value.toString())

            return entries[value]
        }
    }
}