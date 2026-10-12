package usecases.income_sources

import adapters.IAuthenticateAdapter
import usecases.UseCase
import adapters.dto.QueryFilter
import adapters.repositories.IRepository
import adapters.repositories.QueryComparator
import adapters.repositories.QueryExtendBuilder
import domain.entities.IncomeSource
import usecases.dto.ListOutput
import usecases.income_sources.dto.GetAllIncomeSourceInput
import usecases.income_sources.dto.GetIncomeSourceOutput
class GetAllIncomeSource(
    private val incomeSourceRepo: IRepository<IncomeSource>,
    auth: IAuthenticateAdapter
): UseCase<GetAllIncomeSourceInput, ListOutput<GetIncomeSourceOutput>>(auth = auth) {
    override suspend fun process(input: GetAllIncomeSourceInput): ListOutput<GetIncomeSourceOutput> {
        val currentUser = getCurrentUser()
        val condition = QueryExtendBuilder<IncomeSource>()
            .addCondition("userId", QueryComparator.Equal, currentUser.id)
        val incomeSources = incomeSourceRepo.getAll(input.queryFilter, condition)

        return ListOutput(
            items = incomeSources.items.map {
                GetIncomeSourceOutput(
                    id = it.id,
                    title = it.title,
                    type = it.type.value,
                    reliabilityLevel = it.reliabilityLevel,
                    taxRate = it.taxRate,
                    otherRate = it.otherRate,
                    startDate = it.startDate,
                    payFrequencyType = it.payFrequency.value,
                    estimatedFutureOccurrences = it.getEstimateFutureOccurrence(),
                    estimateNextNetAmount = it.getEstimateNextNetAmount(),
                    linkedAccountId = it.linkedAccountId,
                    annualGrossAmount = it.annualGrossAmount,
                    endDate = it.endDate,
                )
            },
            total = incomeSources.total
        )
    }
}