package usecases.goals

import usecases.UseCase
import adapters.IAuthenticateAdapter
import adapters.repositories.IRepository
import domain.exceptions.NotFoundException
import domain.entities.Goal
import java.util.UUID

class DeleteGoal(
    private val goalRepo: IRepository<Goal>,
    auth: IAuthenticateAdapter
): UseCase<UUID, Unit>(auth = auth) {
    override suspend fun process(input: UUID) {
        goalRepo.get(input) ?: throw NotFoundException.SingleEntity(input, "goal")
        goalRepo.delete(input)
    }
}