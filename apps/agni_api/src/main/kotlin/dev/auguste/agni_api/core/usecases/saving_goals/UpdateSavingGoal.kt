package dev.auguste.agni_api.core.usecases.saving_goals

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Account
import dev.auguste.agni_api.core.entities.SavingGoal
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.saving_goals.dto.UpdateSavingGoalInput
import dev.auguste.agni_api.core.value_objects.SavingGoalItem

class UpdateSavingGoal(
    private val savingGoalRepo: IRepository<SavingGoal>,
    private val accountRepo: IRepository<Account>
): IUseCase<UpdateSavingGoalInput, Unit> {

    override fun execAsync(input: UpdateSavingGoalInput) {
        val savingGoal = savingGoalRepo.get(input.id) ?: throw Error("Saving goal not found")

        if (input.title != null) {
            if (input.title != savingGoal.title && savingGoalRepo.existsByName(input.title))
                throw Error("Saving goal '${input.title}' already exists")
        }

        if (input.target != null)
            savingGoal.target = input.target

        if (input.desirValue != null)
            savingGoal.desired = input.desirValue

        if (input.wishDueDate != null)
            savingGoal.wishDueDate = input.wishDueDate

        if (input.description != null)
            savingGoal.description = input.description

        if (input.importance != null)
            savingGoal.importance = input.importance

        if (input.items != null)
            savingGoal.itemsToTracks = input.items.map { SavingGoalItem(
                title = it.title,
                url = it.url,
                price = it.price
            ) }.toMutableSet()

        if (input.accountId != null) {
            if (accountRepo.get(input.accountId) == null)
                throw Error("Account not found")

            savingGoal.accountId = input.accountId
        }

        if (savingGoal.hasChanged())
            savingGoalRepo.update(savingGoal)
    }
}