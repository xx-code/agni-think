package usecases.goals

import adapters.IFinanceContext
import adapters.repositories.IRepository
import domain.exceptions.NotFoundException
import domain.entities.Goal
import domain.factories.GoalEvaluationStrategyFactory
import usecases.goals.dto.UpdateGoalInput
import usecases.interfaces.IUseCase

class UpdateGoal(
    private val goalRepo: IRepository<Goal>,
    private val financeContext: IFinanceContext
): IUseCase<UpdateGoalInput, Unit> {
    override fun execAsync(input: UpdateGoalInput) {
        val goal = goalRepo.get(input.id) ?: throw NotFoundException.SingleEntity(input.id, "goal")

        if (input.status != null && input.status != goal.status) {
            goal.status = input.status
        }

        if (input.title != null && input.title != goal.title) {
            goal.title = input.title
        }

        if (input.description != null && input.description != goal.description) {
            goal.description = input.description
        }

        if (input.targetDate != null && input.targetDate != goal.dueDate) {
            goal.dueDate = input.targetDate
        }

        if (input.targetAmount != null && input.targetAmount != goal.targetAmount) {
            goal.targetAmount = input.targetAmount
        }

        if (goal.hasChanged()) {
            val strategy = GoalEvaluationStrategyFactory.getStrategy(goal.type)
            strategy.verifyGoalBusinessLogic(goal, financeContext)
            goalRepo.update(goal)
        }
    }

}