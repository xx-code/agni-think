package dev.auguste.agni_api.core.usecases.spending_period

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.DomainException
import dev.auguste.agni_api.core.entities.SpendingPeriod
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.spending_period.dto.UpdateSpendingPeriodInput
import dev.auguste.agni_api.core.value_objects.SpendingPeriodItem

class UpdateSpendingPeriod(
    private val spendingPeriodRepo: IRepository<SpendingPeriod>,
): IUseCase<UpdateSpendingPeriodInput, Unit> {
    override fun execAsync(input: UpdateSpendingPeriodInput) {
        val spendPeriod = spendingPeriodRepo.get(input.id) ?: throw DomainException.NotFound.SpendingPeriod(input.id)

        input.startDate?.let { spendPeriod.startDate = it }
        input.endDate?.let { spendPeriod.endDate = it }
        input.suggestionAmount?.let { spendPeriod.suggestionAmount = it }
        input.amount?.let { spendPeriod.amount = it }
        input.wantSpendingItems?.let {
            spendPeriod.wantSpendingItems = it.map { item ->
                SpendingPeriodItem(
                    description = item.description,
                    amount = item.amount
                )
            }
        }

        if (spendPeriod.hasChanged())
            spendingPeriodRepo.update(spendPeriod)
    }
}
