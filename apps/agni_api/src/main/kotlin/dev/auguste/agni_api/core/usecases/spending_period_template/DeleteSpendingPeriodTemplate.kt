package dev.auguste.agni_api.core.usecases.spending_period_template

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.DomainException
import dev.auguste.agni_api.core.entities.SpendingPeriodTemplate
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import java.util.UUID

class DeleteSpendingPeriodTemplate(
    private val spendingPeriodTemplateRepo: IRepository<SpendingPeriodTemplate>,
): IUseCase<UUID, Unit> {
    override fun execAsync(input: UUID) {
        spendingPeriodTemplateRepo.get(input) ?: throw DomainException.NotFound.SpendingPeriodTemplate(input)

        spendingPeriodTemplateRepo.delete(input)
    }
}