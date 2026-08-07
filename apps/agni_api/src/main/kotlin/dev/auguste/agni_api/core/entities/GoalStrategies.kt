package dev.auguste.agni_api.core.entities

import dev.auguste.agni_api.core.adapters.IFinanceContext
import dev.auguste.agni_api.core.entities.enums.GoalEvaluationType
import dev.auguste.agni_api.core.entities.interfaces.IGoalEvaluationStrategy

class FundGoalEvaluationStrategy(
    override val type: GoalEvaluationType = GoalEvaluationType.FUND
) : IGoalEvaluationStrategy {
    override fun evaluateCurrentAmount(
        goal: Goal,
        context: IFinanceContext
    ): Double {
        return context.getFundBalance(goal.targetSourceId)
    }
}

class CategoryEvaluationStrategy(
    override val type: GoalEvaluationType = GoalEvaluationType.TRANSACTION_TARGET
) : IGoalEvaluationStrategy {
    override fun evaluateCurrentAmount(
        goal: Goal,
        context: IFinanceContext
    ): Double {
        return context.getCategoryTotal(goal.targetSourceId, goal.createdAt, goal.dueDate)
    }
}