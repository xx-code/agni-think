package domain.enums

import domain.exceptions.ValidationException

enum class GoalEvaluationType(val value: String) {
    FUND("Fund"),
    TRANSACTION_TARGET("TransactionTarget"),
    PATRIMONY("Patrimony");

    override fun toString(): String {
        return value
    }

    companion object {
        fun fromString(value: String): GoalEvaluationType {
            return entries.find { it.value.equals(value, ignoreCase = true) }
                ?: throw ValidationException.BadType("GoalEvaluationType", value)
        }
    }
}