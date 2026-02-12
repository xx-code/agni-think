package dev.auguste.agni_api.core.usecases.saving_goals

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.SavingGoal
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.saving_goals.dto.GetSavingGoalItemOutput
import dev.auguste.agni_api.core.usecases.saving_goals.dto.GetSavingGoalOutput

class GetAllSavingGoal(private val savingGoalRepo: IRepository<SavingGoal>): IUseCase<QueryFilter, ListOutput<GetSavingGoalOutput>> {
    override fun execAsync(input: QueryFilter): ListOutput<GetSavingGoalOutput> {
        val savingGoals = savingGoalRepo.getAll(input)

        return ListOutput(
            items = savingGoals.items.map {
                GetSavingGoalOutput(
                   id = it.id,
                    title = it.title,
                    description = it.description,
                    target = it.target,
                    balance = it.balance,
                    importance = it.importance.type,
                    wishDueDate = it.wishDueDate,
                    desirValue = it.desired.ordinal,
                    accountId = it.accountId,
                    items = it.itemsToTracks.map { item ->
                        GetSavingGoalItemOutput(
                            title = item.title,
                            url = item.url,
                            price = item.price
                        )
                    }
                )
            },
            total = savingGoals.total
        )
    }
}