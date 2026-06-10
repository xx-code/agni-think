package dev.auguste.agni_api.core.usecases.income_sources

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Account
import dev.auguste.agni_api.core.entities.IncomeSource
import dev.auguste.agni_api.core.usecases.income_sources.dto.UpdateIncomeSourceInput
import dev.auguste.agni_api.core.entities.DomainException
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase

class UpdateIncomeSource(
    private val incomeSourceRepo: IRepository<IncomeSource>,
    private val accountRepo: IRepository<Account>
) : IUseCase<UpdateIncomeSourceInput, Unit> {
    override fun execAsync(input: UpdateIncomeSourceInput) {
        val incomeSource = incomeSourceRepo.get(input.id) ?: throw DomainException.NotFound.IncomeSource(input.id.toString())

        if (input.title != null) {
            if (input.title != incomeSource.title && incomeSourceRepo.existsByName(input.title)) {
                throw DomainException.AlreadyExist.IncomeSource(input.title)
            }

            incomeSource.title = input.title
        }

        if (input.type != null)
            incomeSource.type = input.type

        if (input.payFrequencyType != null)
            incomeSource.payFrequency = input.payFrequencyType

        if (input.otherRate != null)
            incomeSource.otherRate = input.otherRate

        if (input.taxRate != null)
            incomeSource.taxRate = input.taxRate

        if (input.startDate != null)
            incomeSource.startDate = input.startDate

        if (input.endDate != null)
            incomeSource.endDate = input.endDate

        if (input.linkedAccountId != null) {
            if (accountRepo.get(input.linkedAccountId) == null)
                throw DomainException.NotFound.Account(input.linkedAccountId)

            incomeSource.linkedAccountId = input.linkedAccountId
        }

        if (input.annualGrossAmount != null)
            incomeSource.annualGrossAmount = input.annualGrossAmount

        if (input.reliabilityLevel != null) {
            if (input.reliabilityLevel !in 0..100)
                throw DomainException.BusinessLogic.InvalidReliabilityLevel(input.reliabilityLevel)

            incomeSource.reliabilityLevel = input.reliabilityLevel
        }

        if (incomeSource.hasChanged())
            incomeSourceRepo.update(incomeSource)
    }
}