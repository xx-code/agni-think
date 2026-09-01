package dev.auguste.agni_api.core.usecases.spending_period_template

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.SpendingPeriodTemplate
import dev.auguste.agni_api.core.usecases.CreatedOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.spending_period_template.dto.CreateSpendingPeriodInput
import dev.auguste.agni_api.core.value_objects.SchedulerRecurrence

class CreateSpendingPeriodTemplate(
    private val spendingPeriodTemplateRepo: IRepository<SpendingPeriodTemplate>,
): IUseCase<CreateSpendingPeriodInput, CreatedOutput> {
    override fun execAsync(input: CreateSpendingPeriodInput): CreatedOutput {
        val newSpendingPeriodTemplate = SpendingPeriodTemplate(
            startDate = input.startDate,
            recurrence = SchedulerRecurrence(
                period = input.recurrence.period,
                interval = input.recurrence.interval
            ),
            endDate = input.endDate
        )

        spendingPeriodTemplateRepo.create(newSpendingPeriodTemplate)

        return CreatedOutput(newSpendingPeriodTemplate.id)
    }
}