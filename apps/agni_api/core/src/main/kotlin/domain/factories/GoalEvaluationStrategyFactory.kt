package domain.factories

import domain.entities.CategoryEvaluationStrategy
import domain.exceptions.ValidationException
import domain.entities.FundGoalEvaluationStrategy
import domain.enums.GoalEvaluationType
import domain.interfaces.IGoalEvaluationStrategy

class GoalEvaluationStrategyFactory {
    companion object {
        private val strategies: Map<GoalEvaluationType, IGoalEvaluationStrategy> = mapOf(
            GoalEvaluationType.FUND to FundGoalEvaluationStrategy(),
            GoalEvaluationType.TRANSACTION_TARGET to CategoryEvaluationStrategy()
        )

        fun getStrategy(type: GoalEvaluationType): IGoalEvaluationStrategy {
            val strategy = this.strategies[type] ?: throw ValidationException.GoalStrategyNotExist(type)
            return strategy
        }
    }
}