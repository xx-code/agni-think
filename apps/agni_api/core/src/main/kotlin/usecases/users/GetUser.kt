package usecases.users

import adapters.repositories.IRepository
import domain.entities.User
import domain.exceptions.NotFoundException
import usecases.UseCase
import usecases.users.dto.GetUserOutput
import java.util.UUID

class GetUser(
    private val userRepo: IRepository<User>,
): UseCase<UUID, GetUserOutput>() {
    override suspend fun process(input: UUID): GetUserOutput {
        val user = userRepo.get(input) ?: throw NotFoundException.SingleEntity(input, "USER")
        return GetUserOutput(
            userId = user.id,
            userName = user.username,
            email = user.email.toString()
        )
    }
}