package usecases.spending_period

import usecases.UseCase
import adapters.IAuthenticateAdapter
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
    auth: IAuthenticateAdapter
): UseCase<GetAllSpendingPeriodInput, ListOutput<GetAllSpendingPeriodOutput>>(auth = auth) {
    override suspend fun process(input: GetAllSpendingPeriodInput): ListOutput<GetAllSpendingPeriodOutput> {
        val currentUser = getCurrentUser()
        val condition = QueryExtendBuilder<SpendingPeriod>()
            .addCondition("userId", QueryComparator.Equal, currentUser.id)
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
                    savingRateTarget = it.savingRateTarget,
                    state = it.state,
                    snapshot = SpendingPeriodSnapShotOutput(
                        income = it.snapshot.income,
                        fixExpenses = it.snapshot.fixExpenses,
                        variableExpenses = it.snapshot.variableExpenses,
                        budgetExpenses = it.snapshot.budgetExpenses,
                        saving = it.snapshot.saving
                    ),
                    wishlistItems = listOf(),
                )
            },
            total = spendingPeriods.total
        )
    }
}
