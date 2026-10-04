package usecases.goals

import usecases.UseCase
import adapters.IFinanceContext
import adapters.repositories.IRepository
import domain.entities.Goal
import domain.factories.GoalEvaluationStrategyFactory
import usecases.dto.CreatedOutput
import usecases.goals.dto.CreateGoalInput
class CreateGoal(
    private val goalRepo: IRepository<Goal>,
    private val financeContext: IFinanceContext
): UseCase<CreateGoalInput, CreatedOutput>() {
    override suspend fun process(input: CreateGoalInput): CreatedOutput {
        val newGoal = Goal(
            title = input.title,
            description = input.description,
            targetSourceId = input.targetSourceId,
            targetAmount = input.targetAmount,
            dueDate = input.targetDate,
            status = input.status,
            type = input.type,
        )

        val strategy = GoalEvaluationStrategyFactory.getStrategy(newGoal.type)
        strategy.verifyGoalBusinessLogic(newGoal, financeContext)
        newGoal.evaluateProgress(strategy, financeContext)

        goalRepo.create(newGoal)

        return CreatedOutput(newGoal.id)
    }
}