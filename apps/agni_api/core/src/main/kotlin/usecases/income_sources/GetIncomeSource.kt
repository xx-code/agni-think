package usecases.income_sources

import usecases.UseCase
import adapters.repositories.IRepository
import domain.exceptions.NotFoundException
import domain.entities.IncomeSource
import usecases.income_sources.dto.GetIncomeSourceOutput
import java.util.UUID

class GetIncomeSource(
    private val incomeSourceRepo: IRepository<IncomeSource>,
): UseCase<UUID, GetIncomeSourceOutput>() {
    override suspend fun process(input: UUID): GetIncomeSourceOutput {
        val incomeSource = incomeSourceRepo.get(input) ?: throw NotFoundException.SingleEntity(input, "income_source")

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