package dev.auguste.agni_api.core.usecases.spending_period_template

import dev.auguste.agni_api.core.adapters.dto.ScheduleRepeaterOutput
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.DomainException
import dev.auguste.agni_api.core.entities.SpendingPeriodTemplate
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.spending_period_template.dto.GetSpendingPeriodOutput
import java.util.UUID

class GetSpendingPeriodTemplate(
    private val spendingPeriodTemplateRepo: IRepository<SpendingPeriodTemplate>,
): IUseCase<UUID, GetSpendingPeriodOutput>{
    override fun execAsync(input: UUID): GetSpendingPeriodOutput {
        val spendPeriodTemplate = spendingPeriodTemplateRepo.get(input) ?: throw DomainException.NotFound.SpendingPeriodTemplate(input)
        return GetSpendingPeriodOutput(
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