package domain.interfaces

import adapters.IFinanceContext
import domain.entities.Goal
import domain.enums.GoalEvaluationType

interface IGoalEvaluationStrategy {
    val type: GoalEvaluationType
    fun verifyGoalBusinessLogic(goal: Goal, context: IFinanceContext)
    fun evaluateCurrentAmount(goal: Goal, context: IFinanceContext): Double
}