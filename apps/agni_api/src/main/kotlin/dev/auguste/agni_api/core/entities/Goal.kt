package dev.auguste.agni_api.core.entities

import dev.auguste.agni_api.core.adapters.IFinanceContext
import dev.auguste.agni_api.core.entities.enums.GoalEvaluationType
import dev.auguste.agni_api.core.entities.enums.GoalStatusType
import dev.auguste.agni_api.core.entities.interfaces.IGoalEvaluationStrategy
import java.time.LocalDate
import java.util.UUID

data class GoalEvaluationProgress(
    val balance: Double,
    val progressPercent: Double
)

class Goal(
    id: UUID,
    title: String,
    description: String,
    targetSourceId: UUID,
    targetAmount: Double,
    dueDate: LocalDate,
    status: GoalStatusType,
    type: GoalEvaluationType
): Entity(id) {
    var title by cleanObservable(title, this)
    var description by cleanObservable(description, this)
    var targetSourceId by cleanObservable(targetSourceId, this)
    var targetAmount by cleanObservable(targetAmount, this)
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