package dev.auguste.agni_api.core.usecases.goals

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.DomainException
import dev.auguste.agni_api.core.entities.Goal
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import java.util.UUID

class DeleteGoal(
    private val goalRepo: IRepository<Goal>
): IUseCase<UUID, Unit> {
    override fun execAsync(input: UUID) {
        goalRepo.get(input) ?: throw DomainException.NotFound.Goal(input)
        goalRepo.delete(input)
    }
}