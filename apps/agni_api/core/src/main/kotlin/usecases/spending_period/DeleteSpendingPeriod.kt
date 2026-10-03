package usecases.spending_period

import usecases.UseCase
import adapters.repositories.IRepository
import domain.exceptions.NotFoundException
import domain.entities.SpendingPeriod
import java.util.UUID

class DeleteSpendingPeriod(
    private val spendingPeriodRepo: IRepository<SpendingPeriod>,
): UseCase<UUID, Unit>() {
    override suspend fun process(input: UUID) {
        spendingPeriodRepo.get(input) ?: throw NotFoundException.SingleEntity(input, "spending_period")

        spendingPeriodRepo.delete(input)
    }
}
