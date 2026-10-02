package usecases.goals

import adapters.repositories.IRepository
import domain.exceptions.NotFoundException
import domain.entities.Goal
import usecases.interfaces.IUseCase
import java.util.UUID

class DeleteGoal(
    private val goalRepo: IRepository<Goal>
): IUseCase<UUID, Unit> {
    override fun execAsync(input: UUID) {
        goalRepo.get(input) ?: throw NotFoundException.SingleEntity(input, "goal")
        goalRepo.delete(input)
    }
}