package usecases.spending_period

import adapters.repositories.IRepository
import domain.exceptions.NotFoundException
import domain.entities.SpendingPeriod
import usecases.interfaces.IUseCase
import java.util.UUID

class DeleteSpendingPeriod(
    private val spendingPeriodRepo: IRepository<SpendingPeriod>,
): IUseCase<UUID, Unit> {
    override fun execAsync(input: UUID) {
        spendingPeriodRepo.get(input) ?: throw NotFoundException.SingleEntity(input, "spending_period")

        spendingPeriodRepo.delete(input)
    }
}
