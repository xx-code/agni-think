package domain.enums

import domain.exceptions.ValidationException

enum class PriorityRuleLevelType(val value: String) {
    DEBT_ACCEPTABLE("DebtAcceptable"),
    SAVING_FIRST("SavingFirst"),
    LIFE_STYLE_OPTIMIZED("LifeStyleOptimized");

    override fun toString(): String {
        return value
    }

    companion object {
        fun fromString(value: String): PriorityRuleLevelType {
            return entries.find { it.value.equals(value, ignoreCase = true) }
                ?: throw ValidationException.BadType("PriorityRuleLevelType", value)
        }
    }
}