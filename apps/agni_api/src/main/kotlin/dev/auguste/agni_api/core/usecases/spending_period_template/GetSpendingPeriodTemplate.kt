package dev.auguste.agni_api.core.usecases.spending_period_template

import dev.auguste.agni_api.core.adapters.dto.ScheduleRepeaterOutput
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.DomainException
import dev.auguste.agni_api.core.entities.SpendingPeriodTemplate
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.spending_period_template.dto.GetSpendingPeriodTemplateOutput
import java.util.UUID

class GetSpendingPeriodTemplate(
    private val spendingPeriodTemplateRepo: IRepository<SpendingPeriodTemplate>,
): IUseCase<UUID, GetSpendingPeriodTemplateOutput>{
    override fun execAsync(input: UUID): GetSpendingPeriodTemplateOutput {
        val spendPeriodTemplate = spendingPeriodTemplateRepo.get(input) ?: throw DomainException.NotFound.SpendingPeriodTemplate(input)
        return GetSpendingPeriodTemplateOutput(
            id = spendPeriodTemplate.id,
            recurrence = ScheduleRepeaterOutput(
                spendPeriodTemplate.recurrence.period.value,
                spendPeriodTemplate.recurrence.interval
            ),
            isActive = spendPeriodTemplate.checkIsActive(),
            startDate = spendPeriodTemplate.startDate,
            endDate = spendPeriodTemplate.endDate
        )
    }
}