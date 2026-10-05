package domain.entities

import adapters.IFinanceContext
import domain.enums.GoalEvaluationType
import domain.enums.GoalStatusType
import domain.exceptions.ValidationException
import domain.interfaces.IGoalEvaluationStrategy
import domain.value_objects.SchedulerRecurrence
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
    recurrence: SchedulerRecurrence? = null
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
    var recurrence by cleanObservable(recurrence, this)

    suspend fun evaluateProgress(strategy: IGoalEvaluationStrategy, context: IFinanceContext): GoalEvaluationProgress {
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

    suspend fun isComplete(strategy: IGoalEvaluationStrategy, context: IFinanceContext, date: LocalDate = LocalDate.now()): Boolean {
        val evalutionProgress = evaluateProgress(strategy, context)

        return evalutionProgress.progressPercent == 100.0 && date >= dueDate
    }
}