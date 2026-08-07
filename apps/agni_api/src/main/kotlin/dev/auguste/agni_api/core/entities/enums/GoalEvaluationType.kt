package dev.auguste.agni_api.core.entities.enums

enum class GoalEvaluationType(val value: String) {
    FUND("Fund"),
    TRANSACTION_TARGET("TransactionTarget"),
    PATRIMONY("Patrimony");

    companion object {
        fun fromString(value: String): GoalEvaluationType {
            return GoalEvaluationType.entries.find { it.value.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("Goal Evaluation Type $value not found in enums")
        }
    }
}