package dev.auguste.agni_api.core.usecases.saving_goals

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.SavingGoal
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.saving_goals.dto.GetSavingGoalItemOutput
import dev.auguste.agni_api.core.usecases.saving_goals.dto.GetSavingGoalOutput
import java.util.UUID

class GetSavingGoal(private val savingGoalRepo: IRepository<SavingGoal>): IUseCase<UUID, GetSavingGoalOutput> {

    override fun execAsync(input: UUID): GetSavingGoalOutput {
        val savingGoal = savingGoalRepo.get(input) ?: throw Error("Saving goal not found")

        return GetSavingGoalOutput(
            id = savingGoal.id,
            title = savingGoal.title,
            description = savingGoal.description,
            target = savingGoal.target,
            balance = savingGoal.balance,
            importance = savingGoal.importance,
            wishDueDate = savingGoal.wishDueDate,
            desireValue = savingGoal.desired,
            accountId = savingGoal.accountId,
            items = savingGoal.itemsToTracks.map {
                GetSavingGoalItemOutput(
                    title = it.title,
                    url = it.url,
                    price = it.price
                )
            }
        )
    }
}