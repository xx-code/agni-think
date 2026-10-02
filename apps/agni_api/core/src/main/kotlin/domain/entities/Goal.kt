package domain.entities

import adapters.IFinanceContext
import domain.enums.GoalEvaluationType
import domain.enums.GoalStatusType
import domain.exceptions.ValidationException
import domain.interfaces.IGoalEvaluationStrategy
import java.time.LocalDate
import java.util.UUID


data class GoalEvaluationProgress(
    val balance: Double,
    val progressPercent: Double
)

class Goal(
    id: UUID = UUID.randomUUID(),
    title: String,
    description: String,
    targetSourceId: UUID,
    targetAmount: Double,
    dueDate: LocalDate,
    status: GoalStatusType,
    type: GoalEvaluationType,
): Entity(id) {
    var title by cleanObservable(title, this)
    var description by cleanObservable(description, this)
    var targetSourceId by cleanObservable(targetSourceId, this)
    var targetAmount by cleanObservable(targetAmount, this, {
        it >= 0.0
    }) {
        ValidationException.GoalTargetAmountMustBeGreaterThanZero(it)
    }
    var dueDate by cleanObservable(dueDate, this)
    var status by cleanObservable(status, this)
    var type by cleanObservable(type, this)

    fun evaluateProgress(strategy: IGoalEvaluationStrategy, context: IFinanceContext): GoalEvaluationProgress {
        val currentAmount = strategy.evaluateCurrentAmount(this, context)

        if (targetAmount == 0.0)
            return GoalEvaluationProgress(
                currentAmount,
                0.0
            )

        return GoalEvaluationProgress(
            currentAmount,
            ((currentAmount / this.targetAmount) * 100).coerceAtMost(100.0)
        )
    }
}