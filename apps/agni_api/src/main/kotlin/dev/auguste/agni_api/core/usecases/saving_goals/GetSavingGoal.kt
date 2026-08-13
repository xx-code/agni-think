package dev.auguste.agni_api.core.usecases.saving_goals

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryGoalExtend
import dev.auguste.agni_api.core.entities.SavingGoal
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.entities.DomainException
import dev.auguste.agni_api.core.entities.Goal
import dev.auguste.agni_api.core.usecases.saving_goals.dto.FundGoalOutput
import dev.auguste.agni_api.core.usecases.saving_goals.dto.GetSavingGoalOutput
import java.util.UUID

class GetSavingGoal(
    private val savingGoalRepo: IRepository<SavingGoal>,
    private val goalRepo: IRepository<Goal>
    ): IUseCase<UUID, GetSavingGoalOutput> {

    override fun execAsync(input: UUID): GetSavingGoalOutput {
        val savingGoal = savingGoalRepo.get(input) ?: throw DomainException.NotFound.SavingGoal(input)
        val goals = goalRepo.getAll(QueryFilter.queryAll(), QueryGoalExtend(sourceIds = setOf(savingGoal.id) ))

        return GetSavingGoalOutput(
            id = savingGoal.id,
            title = savingGoal.title,
            description = savingGoal.description,
            target = savingGoal.target,
            balance = savingGoal.balance,
            accountId = savingGoal.accountId,
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