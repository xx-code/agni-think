package usecases.spending_period

import usecases.UseCase
import adapters.repositories.IRepository
import adapters.repositories.QueryExtendBuilder
import adapters.repositories.QueryComparator
import domain.entities.SpendingPeriod
import usecases.dto.ListOutput
import usecases.spending_period.dto.GetAllSpendingPeriodInput
import usecases.spending_period.dto.GetAllSpendingPeriodOutput
import usecases.spending_period.dto.SpendingPeriodItemOutput
import usecases.spending_period.dto.SpendingPeriodSnapShotOutput

class GetAllSpendingPeriod(
    private val spendingPeriodRepo: IRepository<SpendingPeriod>,
): UseCase<GetAllSpendingPeriodInput, ListOutput<GetAllSpendingPeriodOutput>>() {
    override suspend fun process(input: GetAllSpendingPeriodInput): ListOutput<GetAllSpendingPeriodOutput> {
        val condition = QueryExtendBuilder<SpendingPeriod>()
            .addCondition("spendingPeriodTemplateId", QueryComparator.Equal, input.spendingPeriodTemplateId)
            .addCondition("state", QueryComparator.Equal, input.state?.value)
//            .addCondition("startDate", QueryComparator.Equal, input.startDate)
//            .addCondition("endDate", QueryComparator.Equal, input.endDate)


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
