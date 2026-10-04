package usecases.goals

import usecases.UseCase
import adapters.IFinanceContext
import adapters.repositories.IRepository
import domain.exceptions.NotFoundException
import domain.entities.Goal
import domain.factories.GoalEvaluationStrategyFactory
import usecases.goals.dto.GetGoalEvaluationOutput
import usecases.goals.dto.GetGoalOutput
import java.util.UUID

class GetGoal(
    private val goalRepo: IRepository<Goal>,
    private val financeContext: IFinanceContext
): UseCase<UUID, GetGoalOutput>() {
    override suspend fun process(input: UUID): GetGoalOutput {
        val goal = goalRepo.get(input) ?: throw NotFoundException.SingleEntity(input, "goal")

        val strategy = GoalEvaluationStrategyFactory.getStrategy(goal.type)
        val evaluation = goal.evaluateProgress(strategy, financeContext)

        return GetGoalOutput(
            id = goal.id,
            title = goal.title,
            description = goal.description,
            targetAmount = goal.targetAmount,
            targetSourceId = goal.targetSourceId,
            dueDate = goal.dueDate,
            createdDate = goal.createdAt.toLocalDate(),
            status = goal.status,
            type = goal.type,
            evaluation = GetGoalEvaluationOutput(
                currentBalance = evaluation.balance,
                progressPercentage = evaluation.progressPercent
            )
        )
    }
}