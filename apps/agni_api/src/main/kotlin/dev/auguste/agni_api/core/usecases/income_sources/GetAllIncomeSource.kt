package dev.auguste.agni_api.core.usecases.income_sources

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.IncomeSource
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.income_sources.dto.GetIncomeSourceOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import java.time.LocalDate

class GetAllIncomeSource(
    private val incomeSourceRepo: IRepository<IncomeSource>,
) : IUseCase<QueryFilter, ListOutput<GetIncomeSourceOutput>> {
    override fun execAsync(input: QueryFilter): ListOutput<GetIncomeSourceOutput> {
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