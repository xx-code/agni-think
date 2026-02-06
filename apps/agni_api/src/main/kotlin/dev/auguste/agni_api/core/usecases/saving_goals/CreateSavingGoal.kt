package dev.auguste.agni_api.core.usecases.saving_goals

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Account
import dev.auguste.agni_api.core.entities.SavingGoal
import dev.auguste.agni_api.core.usecases.CreatedOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.savingGoals.dto.CreateSavingGoalInput
import dev.auguste.agni_api.core.value_objects.SavingGoalItem

class CreateSavingGoal(
    val savingGoalRepo: IRepository<SavingGoal>,
    val accountingRepo: IRepository<Account>): IUseCase<CreateSavingGoalInput, CreatedOutput> {

    override fun execAsync(input: CreateSavingGoalInput): CreatedOutput {
        if (input.accountId != null && this.accountingRepo.get(input.accountId) == null)
            throw Error("Account with id ${input.accountId} not found")

        if (input.target < 0)
            throw Error("Target must be positive")

        if (savingGoalRepo.existsByName(input.title))
            throw Error("The title ${input.title} already exists")

        val newSavingGoal = SavingGoal(
            title = input.title,
            description = input.description,
            accountId = input.accountId,
            target = input.target,
            balance = 0.0,
            desired = input.desirValue,
            importance = input.importance,
            wishDueDate = input.wishDueDate,
            itemsToTracks = input.items.map {
                SavingGoalItem(
                    title = it.title,
                    price = it.price,
                    url = it.url
                )
            }.toMutableSet()
        )

        savingGoalRepo.create(newSavingGoal)

        return CreatedOutput(newSavingGoal.id)
    }
}