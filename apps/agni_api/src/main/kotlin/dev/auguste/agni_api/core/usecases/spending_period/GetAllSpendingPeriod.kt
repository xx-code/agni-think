package dev.auguste.agni_api.core.usecases.spending_period

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.SpendingPeriod
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.spending_period.dto.GetSpendingPeriodOutput
import dev.auguste.agni_api.core.usecases.spending_period.dto.SpendingPeriodItemOutput

class GetAllSpendingPeriod(
    private val spendingPeriodRepo: IRepository<SpendingPeriod>,
): IUseCase<QueryFilter, ListOutput<GetSpendingPeriodOutput>> {
    override fun execAsync(input: QueryFilter): ListOutput<GetSpendingPeriodOutput> {
        val spendingPeriods = spendingPeriodRepo.getAll(input)

        return ListOutput(
            items = spendingPeriods.items.map {
                GetSpendingPeriodOutput(
                    id = it.id,
                    spendingPeriodTemplateId = it.spendingPeriodTemplateId,
                    startDate = it.startDate,
                    endDate = it.endDate,
                    suggestionAmount = it.suggestionAmount,
                    amount = it.amount,
                    wantSpendingItems = it.wantSpendingItems.map { item ->
                        SpendingPeriodItemOutput(
                            description = item.description,
                            amount = item.amount
                        )
                    }
                )
            },
            total = spendingPeriods.total
        )
    }
}
