package dev.auguste.agni_api.core.usecases.saving_goals

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Account
import dev.auguste.agni_api.core.entities.DomainException
import dev.auguste.agni_api.core.entities.SavingGoal
import dev.auguste.agni_api.core.usecases.CreatedOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.saving_goals.dto.CreateSavingGoalInput

class CreateSavingGoal(
    private val savingGoalRepo: IRepository<SavingGoal>,
    private val accountingRepo: IRepository<Account>): IUseCase<CreateSavingGoalInput, CreatedOutput> {

    override fun execAsync(input: CreateSavingGoalInput): CreatedOutput {
        if (input.accountId != null && this.accountingRepo.get(input.accountId) == null)
            throw DomainException.NotFound.Account(input.accountId)

        if (input.target < 0)
            throw DomainException.BusinessLogic.Validation("Target must be positive")

        if (savingGoalRepo.existsByName(input.title))
            throw DomainException.AlreadyExist.SavingGoal(input.title)

        val newSavingGoal = SavingGoal(
            title = input.title,
            description = input.description,
            accountId = input.accountId,
            target = input.target,
            balance = 0.0
        )

        savingGoalRepo.create(newSavingGoal)

        return CreatedOutput(newSavingGoal.id)
    }
}