package dev.auguste.agni_api.core.entities.enums

enum class PriorityRuleLevelType(val value: String) {
    DEBT_ACCEPTABLE("DebtAcceptable"),
    SAVING_FIRST("SavingFirst"),
    LIFE_STYLE_OPTIMIZED("LifeStyleOptimized");

    companion object {
        fun fromString(value: String): PriorityRuleLevelType {
            return PriorityRuleLevelType.entries.find { it.value.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("Priority Rule Level $value not found in enums")
        }
    }
}