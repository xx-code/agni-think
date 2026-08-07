package dev.auguste.agni_api.core.entities.interfaces

import dev.auguste.agni_api.core.adapters.IFinanceContext
import dev.auguste.agni_api.core.entities.Goal
import dev.auguste.agni_api.core.entities.enums.GoalEvaluationType

interface IGoalEvaluationStrategy {
    val type: GoalEvaluationType
    fun evaluateCurrentAmount(goal: Goal, context: IFinanceContext): Double
}