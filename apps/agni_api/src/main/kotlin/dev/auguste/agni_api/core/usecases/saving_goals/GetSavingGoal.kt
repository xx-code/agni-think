package dev.auguste.agni_api.core.usecases.saving_goals

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.SavingGoal
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.entities.DomainException
import dev.auguste.agni_api.core.usecases.saving_goals.dto.GetSavingGoalOutput
import java.util.UUID

class GetSavingGoal(private val savingGoalRepo: IRepository<SavingGoal>): IUseCase<UUID, GetSavingGoalOutput> {

    override fun execAsync(input: UUID): GetSavingGoalOutput {
        val savingGoal = savingGoalRepo.get(input) ?: throw DomainException.NotFound.SavingGoal(input)

        return GetSavingGoalOutput(
            id = savingGoal.id,
            title = savingGoal.title,
            description = savingGoal.description,
            target = savingGoal.target,
            balance = savingGoal.balance,
            accountId = savingGoal.accountId
        )
    }
}