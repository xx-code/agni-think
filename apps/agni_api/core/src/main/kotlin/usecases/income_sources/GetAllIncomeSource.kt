package usecases.income_sources

import usecases.UseCase
import adapters.dto.QueryFilter
import adapters.repositories.IRepository
import domain.entities.IncomeSource
import usecases.dto.ListOutput
import usecases.income_sources.dto.GetIncomeSourceOutput
class GetAllIncomeSource(
    private val incomeSourceRepo: IRepository<IncomeSource>,
): UseCase<QueryFilter, ListOutput<GetIncomeSourceOutput>>() {
    override suspend fun process(input: QueryFilter): ListOutput<GetIncomeSourceOutput> {
        val incomeSources = incomeSourceRepo.getAll(input)

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