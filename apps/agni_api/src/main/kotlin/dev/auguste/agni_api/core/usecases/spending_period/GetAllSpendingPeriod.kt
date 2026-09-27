package dev.auguste.agni_api.core.usecases.spending_period

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.adapters.repositories.QueryExtendBuilder
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryComparator
import dev.auguste.agni_api.core.entities.SpendingPeriod
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.spending_period.dto.GetAllSpendingPeriodInput
import dev.auguste.agni_api.core.usecases.spending_period.dto.GetAllSpendingPeriodOutput
import dev.auguste.agni_api.core.usecases.spending_period.dto.GetSpendingPeriodOutput
import dev.auguste.agni_api.core.usecases.spending_period.dto.SpendingPeriodItemOutput
import dev.auguste.agni_api.core.usecases.spending_period.dto.SpendingPeriodSnapShotOutput

class GetAllSpendingPeriod(
    private val spendingPeriodRepo: IRepository<SpendingPeriod>,
): IUseCase<GetAllSpendingPeriodInput, ListOutput<GetAllSpendingPeriodOutput>> {
    override fun execAsync(input: GetAllSpendingPeriodInput): ListOutput<GetAllSpendingPeriodOutput> {
        val condition = QueryExtendBuilder<SpendingPeriod>()
        if (input.spendingPeriodTemplateId != null)
            condition.addCondition("spendingPeriodTemplateId", QueryComparator.Equal, input.spendingPeriodTemplateId)

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
                GetAllSpendingPeriodOutput(
                    id = it.id,
                    spendingPeriodTemplateId = it.spendingPeriodTemplateId,
                    startDate = it.startDate,
                    endDate = it.endDate,
                    freeAmount = it.freeAmount,
                    savingRateTarget = it.savingRateTarget,
                    totalExpectedIncome = it.totalExpectedIncome,
                    totalExpectedExpenses = it.totalExpectedExpenses,
                    state = it.state,
                    snapshot = SpendingPeriodSnapShotOutput(
                        income = it.snapshot.income,
                        fixExpenses = it.snapshot.fixExpenses,
                        variableExpenses = it.snapshot.variableExpenses,
                        budgetExpenses = it.snapshot.budgetExpenses,
                        saving = it.snapshot.saving
                    ),
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
