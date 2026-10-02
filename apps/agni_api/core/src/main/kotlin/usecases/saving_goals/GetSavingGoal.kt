package usecases.saving_goals

import adapters.dto.QueryFilter
import adapters.repositories.IRepository
import adapters.repositories.query_extend.QueryGoalExtend
import domain.entities.Fund
import usecases.interfaces.IUseCase
import domain.exceptions.NotFoundException
import domain.entities.Goal
import usecases.saving_goals.dto.FundGoalOutput
import usecases.saving_goals.dto.GetSavingGoalOutput
import java.util.UUID

class GetSavingGoal(
    private val fundRepo: IRepository<Fund>,
    private val goalRepo: IRepository<Goal>
    ): IUseCase<UUID, GetSavingGoalOutput> {

    override fun execAsync(input: UUID): GetSavingGoalOutput {
        val savingGoal = fundRepo.get(input) ?: throw NotFoundException.SingleEntity(input, "saving_goal")
        val goals = goalRepo.getAll(QueryFilter.queryAll(), QueryGoalExtend(sourceIds = setOf(savingGoal.id) ))

        return GetSavingGoalOutput(
            id = savingGoal.id,
            title = savingGoal.title,
            description = savingGoal.description,
            target = savingGoal.target,
            balance = savingGoal.balance,
            accountId = savingGoal.accountId,
            type = savingGoal.type.value,
            goals = goals.items.map {
                FundGoalOutput(
                    id = it.id,
                    title = it.title,
                    dueDate = it.dueDate
                )
            }
        )
    }
}