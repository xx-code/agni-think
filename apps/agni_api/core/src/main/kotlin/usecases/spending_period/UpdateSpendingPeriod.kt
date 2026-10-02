package usecases.spending_period

import adapters.repositories.IRepository
import domain.exceptions.NotFoundException
import domain.entities.SpendingPeriod
import usecases.interfaces.IUseCase
import usecases.spending_period.dto.UpdateSpendingPeriodInput
import domain.value_objects.SpendingPeriodItem

class UpdateSpendingPeriod(
    private val spendingPeriodRepo: IRepository<SpendingPeriod>,
): IUseCase<UpdateSpendingPeriodInput, Unit> {
    override fun execAsync(input: UpdateSpendingPeriodInput) {
        val spendPeriod = spendingPeriodRepo.get(input.id) ?: throw NotFoundException.SingleEntity(input.id, "spending_period")

        input.startDate?.let { spendPeriod.startDate = it }
        input.endDate?.let { spendPeriod.endDate = it }
        input.freeAmount?.let { spendPeriod.freeAmount = it }
        input.savingRateTarget?.let { spendPeriod.savingRateTarget = it }
        input.totalExpectedIncome?.let { spendPeriod.totalExpectedIncome = it }
        input.totalExpectedExpenses?.let { spendPeriod.totalExpectedExpenses = it }
        input.state?.let { spendPeriod.state = it }
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
