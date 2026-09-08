package dev.auguste.agni_api.core.usecases.spending_period

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.DomainException
import dev.auguste.agni_api.core.entities.SpendingPeriod
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.spending_period.dto.GetSpendingPeriodOutput
import dev.auguste.agni_api.core.usecases.spending_period.dto.SpendingPeriodItemOutput
import java.util.UUID

class GetSpendingPeriod(
    private val spendingPeriodRepo: IRepository<SpendingPeriod>,
): IUseCase<UUID, GetSpendingPeriodOutput> {
    override fun execAsync(input: UUID): GetSpendingPeriodOutput {
        val spendPeriod = spendingPeriodRepo.get(input) ?: throw DomainException.NotFound.SpendingPeriod(input)
        return GetSpendingPeriodOutput(
            id = spendPeriod.id,
            spendingPeriodTemplateId = spendPeriod.spendingPeriodTemplateId,
            startDate = spendPeriod.startDate,
            endDate = spendPeriod.endDate,
            suggestionAmount = spendPeriod.suggestionAmount,
            savingsTarget = spendPeriod.savingsTarget,
            totalExpectedIncome = spendPeriod.totalExpectedIncome,
            totalExpectedExpenses = spendPeriod.totalExpectedExpenses,
            state = spendPeriod.state,
            wantSpendingItems = spendPeriod.wantSpendingItems.map {
                SpendingPeriodItemOutput(
                    description = it.description,
                    amount = it.amount
                )
            }
        )
    }
}
