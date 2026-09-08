package dev.auguste.agni_api.core.usecases.spending_period

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.adapters.repositories.QueryExtendBuilder
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryComparator
import dev.auguste.agni_api.core.entities.SpendingPeriod
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.spending_period.dto.GetAllSpendingPeriodInput
import dev.auguste.agni_api.core.usecases.spending_period.dto.GetSpendingPeriodOutput
import dev.auguste.agni_api.core.usecases.spending_period.dto.SpendingPeriodItemOutput

class GetAllSpendingPeriod(
    private val spendingPeriodRepo: IRepository<SpendingPeriod>,
): IUseCase<GetAllSpendingPeriodInput, ListOutput<GetSpendingPeriodOutput>> {
    override fun execAsync(input: GetAllSpendingPeriodInput): ListOutput<GetSpendingPeriodOutput> {
        val condition = QueryExtendBuilder<SpendingPeriod>()
        if (input.spendingPeriodId != null)
            condition.addCondition("spendingPeriodId", QueryComparator.Equal, input.spendingPeriodId)

//        if (input.startDate != null)
//            condition.addCondition("startDate", QueryComparator.Equal, input.startDate)
//
//        if (input.endDate != null)
//            condition.addCondition("endDate", QueryComparator.Equal, input.endDate)
//
        if (input.state != null)
            condition.addCondition("state", QueryComparator.Equal, input.state)

        val spendingPeriods = spendingPeriodRepo.getAll(input.queryFilter, condition)

        return ListOutput(
            items = spendingPeriods.items.map {
                GetSpendingPeriodOutput(
                    id = it.id,
                    spendingPeriodTemplateId = it.spendingPeriodTemplateId,
                    startDate = it.startDate,
                    endDate = it.endDate,
                    suggestionAmount = it.suggestionAmount,
                    savingsTarget = it.savingsTarget,
                    totalExpectedIncome = it.totalExpectedIncome,
                    totalExpectedExpenses = it.totalExpectedExpenses,
                    state = it.state,
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
