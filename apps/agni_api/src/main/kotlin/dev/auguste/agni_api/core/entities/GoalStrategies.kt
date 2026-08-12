package dev.auguste.agni_api.core.entities

import dev.auguste.agni_api.core.adapters.IFinanceContext
import dev.auguste.agni_api.core.entities.enums.GoalEvaluationType
import dev.auguste.agni_api.core.entities.interfaces.IGoalEvaluationStrategy

class FundGoalEvaluationStrategy(
    override val type: GoalEvaluationType = GoalEvaluationType.FUND
) : IGoalEvaluationStrategy {
    override fun verifyGoalBusinessLogic(
        goal: Goal,
        context: IFinanceContext
    ) {
        val fund = context.getFund(goal.targetSourceId)
        if (goal.targetAmount > fund.balance)
            throw DomainException.BusinessLogic.GoalTargetAmountMustBeLeastFund(fund.balance, goal.targetAmount)
    }

    override fun evaluateCurrentAmount(
        goal: Goal,
        context: IFinanceContext
    ): Double {
        val fund = context.getFund(goal.targetSourceId)
        return fund.balance
    }
}

class CategoryEvaluationStrategy(
    override val type: GoalEvaluationType = GoalEvaluationType.TRANSACTION_TARGET
) : IGoalEvaluationStrategy {
    override fun verifyGoalBusinessLogic(
        goal: Goal,
        context: IFinanceContext
    ) {
        context.verifyCategoryExists(goal.targetSourceId)
    }

    override fun evaluateCurrentAmount(
        goal: Goal,
        context: IFinanceContext
    ): Double {
        return context.getCategoryTotal(goal.targetSourceId, goal.createdAt, goal.dueDate)
    }
}