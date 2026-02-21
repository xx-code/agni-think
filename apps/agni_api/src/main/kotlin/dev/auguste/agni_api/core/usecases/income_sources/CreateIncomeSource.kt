package dev.auguste.agni_api.core.usecases.income_sources

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Account
import dev.auguste.agni_api.core.entities.IncomeSource
import dev.auguste.agni_api.core.usecases.CreatedOutput
import dev.auguste.agni_api.core.usecases.income_sources.dto.CreateIncomeSourceInput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase

class CreateIncomeSource(
    private val incomeSourceRepo: IRepository<IncomeSource>,
    private val accountRepo: IRepository<Account>
) : IUseCase<CreateIncomeSourceInput, CreatedOutput> {
    override fun execAsync(input: CreateIncomeSourceInput): CreatedOutput {
        if (incomeSourceRepo.existsByName(input.title))
            throw Error("The income source ${input.title} already exists.")

        if (input.linkedAccountId != null) {
            if (accountRepo.get(input.linkedAccountId) == null)
                throw Error("Account ${input.linkedAccountId} does not exist.")
        }

        if (input.reliabilityLevel !in 0..100)
            throw Error("The reliability level must be between 0 and 100.")

        val newIncomeSource = IncomeSource(
            title = input.title,
            type = input.type,
            payFrequency = input.payFrequencyType,
            reliabilityLevel = input.reliabilityLevel,
            startDate = input.startDate,
            taxRate = input.taxRate,
            otherRate = input.otherRate,
            linkedAccountId = input.linkedAccountId,
            annualGrossAmount = input.annualGrossAmount,
            endDate = input.endDate,
        )

        incomeSourceRepo.create(newIncomeSource)

        return CreatedOutput(newIncomeSource.id)
    }
}