package dev.auguste.agni_api.core.usecases.spending_period_template

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.dto.ScheduleRepeaterOutput
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.SpendingPeriodTemplate
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.spending_period_template.dto.GetSpendingPeriodTemplateOutput

class GetAllSpendingPeriodTemplate(
    private val spendingPeriodTemplateRepo: IRepository<SpendingPeriodTemplate>,
): IUseCase<QueryFilter, ListOutput<GetSpendingPeriodTemplateOutput>> {
    override fun execAsync(input: QueryFilter): ListOutput<GetSpendingPeriodTemplateOutput> {
        val spendingPeriodTemplate = spendingPeriodTemplateRepo.getAll(input)

        return ListOutput(
            items = spendingPeriodTemplate.items.map {
                GetSpendingPeriodTemplateOutput(
                    id = it.id,
                    recurrence = ScheduleRepeaterOutput(
                        period = it.recurrence.period.value,
                        interval = it.recurrence.interval,
                    ),
                    isActive = it.checkIsActive(),
                    startDate = it.startDate,
                    endDate = it.endDate
                )
            },
            total = spendingPeriodTemplate.total
        )
    }
}