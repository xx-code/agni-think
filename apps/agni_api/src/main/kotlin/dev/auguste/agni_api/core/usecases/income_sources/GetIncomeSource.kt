package dev.auguste.agni_api.core.usecases.income_sources

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.IncomeSource
import dev.auguste.agni_api.core.usecases.income_sources.dto.GetIncomeSourceOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import java.util.UUID

class GetIncomeSource(
    private val incomeSourceRepo: IRepository<IncomeSource>,
) : IUseCase<UUID, GetIncomeSourceOutput> {
    override fun execAsync(input: UUID): GetIncomeSourceOutput {
        val incomeSource = incomeSourceRepo.get(input) ?: throw IllegalArgumentException("Income source $input not found")

        return GetIncomeSourceOutput(
            id = incomeSource.id,
            title = incomeSource.title,
            type = incomeSource.type.value,
            reliabilityLevel = incomeSource.reliabilityLevel,
            taxRate = incomeSource.taxRate,
            otherRate = incomeSource.otherRate,
            startDate = incomeSource.startDate,
            payFrequencyType = incomeSource.payFrequency.value,
            estimatedFutureOccurrences = incomeSource.getEstimateFutureOccurrence(),
            estimateNextNetAmount = incomeSource.getEstimateNextNetAmount(),
            linkedAccountId = incomeSource.linkedAccountId,
            annualGrossAmount = incomeSource.annualGrossAmount,
            endDate = incomeSource.endDate,
        )
    }
}