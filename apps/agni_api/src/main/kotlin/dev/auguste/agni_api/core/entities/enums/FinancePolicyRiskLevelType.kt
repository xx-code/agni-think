package dev.auguste.agni_api.core.entities.enums

enum class FinancePolicyRiskLevelType {
    LOW,
    MEDIUM,
    HIGH;

    companion object {
        fun fromInt(value: Int): FinancePolicyRiskLevelType {
            if (value !in 0..3)
                throw IllegalArgumentException("Invalid FinancePolicyRiskLevel value $value")

            return FinancePolicyRiskLevelType.entries[value]
        }
    }
}