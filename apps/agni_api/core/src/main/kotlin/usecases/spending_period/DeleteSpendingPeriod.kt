package usecases.spending_period

import usecases.UseCase
import adapters.IAuthenticateAdapter
import adapters.repositories.IRepository
import domain.exceptions.NotFoundException
import domain.entities.SpendingPeriod
import java.util.UUID

class DeleteSpendingPeriod(
    private val spendingPeriodRepo: IRepository<SpendingPeriod>,
    auth: IAuthenticateAdapter
): UseCase<UUID, Unit>(auth = auth) {
    override suspend fun process(input: UUID) {
        spendingPeriodRepo.get(input) ?: throw NotFoundException.SingleEntity(input, "spending_period")

        spendingPeriodRepo.delete(input)
    }
}
