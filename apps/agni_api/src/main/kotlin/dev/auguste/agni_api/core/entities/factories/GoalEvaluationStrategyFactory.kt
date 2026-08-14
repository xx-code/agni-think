package dev.auguste.agni_api.core.entities.factories

import dev.auguste.agni_api.core.entities.CategoryEvaluationStrategy
import dev.auguste.agni_api.core.entities.DomainException
import dev.auguste.agni_api.core.entities.FundGoalEvaluationStrategy
import dev.auguste.agni_api.core.entities.enums.GoalEvaluationType
import dev.auguste.agni_api.core.entities.interfaces.IGoalEvaluationStrategy

class GoalEvaluationStrategyFactory {
    companion object {
        private val strategies: Map<GoalEvaluationType, IGoalEvaluationStrategy> = mapOf(
            GoalEvaluationType.FUND to FundGoalEvaluationStrategy(),
            GoalEvaluationType.TRANSACTION_TARGET to CategoryEvaluationStrategy()
        )

        fun getStrategy(type: GoalEvaluationType): IGoalEvaluationStrategy {
            val strategy = this.strategies[type] ?: throw DomainException.BusinessLogic.GoalStrategyNotExist(type)
            return strategy
        }
    }
}