package dev.auguste.agni_api.core.usecases.spending_period

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.DomainException
import dev.auguste.agni_api.core.entities.SpendingPeriod
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import java.util.UUID

class DeleteSpendingPeriod(
    private val spendingPeriodRepo: IRepository<SpendingPeriod>,
): IUseCase<UUID, Unit> {
    override fun execAsync(input: UUID) {
        spendingPeriodRepo.get(input) ?: throw DomainException.NotFound.SpendingPeriod(input)

        spendingPeriodRepo.delete(input)
    }
}
