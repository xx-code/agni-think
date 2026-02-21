package dev.auguste.agni_api.core.usecases.income_sources

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.IncomeSource
import dev.auguste.agni_api.core.usecases.income_sources.dto.DeleteIncomeSourceInput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase

class DeleteIncomeSource(
    private val incomeSourceRepo: IRepository<IncomeSource>,
) : IUseCase<DeleteIncomeSourceInput, Unit> {
    override fun execAsync(input: DeleteIncomeSourceInput) {
        incomeSourceRepo.get(input.incomeSourceId) ?: throw IllegalArgumentException("Income source ${input.incomeSourceId} not found")
        incomeSourceRepo.delete(input.incomeSourceId)
    }
}