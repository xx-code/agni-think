package dev.auguste.agni_api.core.usecases.spending_period_template

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.DomainException
import dev.auguste.agni_api.core.entities.SpendingPeriodTemplate
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.spending_period_template.dto.UpdateSpendingPeriodInput
import dev.auguste.agni_api.core.value_objects.SchedulerRecurrence

class UpdateSpendingPeriodTemplate(
    private val spendingPeriodTemplateRepo: IRepository<SpendingPeriodTemplate>,
): IUseCase<UpdateSpendingPeriodInput, Unit> {
    override fun execAsync(input: UpdateSpendingPeriodInput) {
        val spendPeriodTemplate = spendingPeriodTemplateRepo.get(input.id) ?: throw DomainException.NotFound.SpendingPeriodTemplate(input.id)

        if (input.recurrence != null) {
            spendPeriodTemplate.recurrence = SchedulerRecurrence(
                period = input.recurrence.period,
                interval = input.recurrence.interval,
            )
        }

        if (input.startDate != null) {
            spendPeriodTemplate.startDate = input.startDate
        }

        if (input.endDate != null) {
            spendPeriodTemplate.endDate = input.endDate
        }

        if (input.isActive != null)
            spendPeriodTemplate.isActive = input.isActive

        if (spendPeriodTemplate.hasChanged())
            spendingPeriodTemplateRepo.update(spendPeriodTemplate)
    }
}