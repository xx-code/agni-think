package dev.auguste.agni_api.core.usecases.spending_period_template

import dev.auguste.agni_api.core.adapters.dto.ScheduleRepeaterOutput
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Budget
import dev.auguste.agni_api.core.entities.DomainException
import dev.auguste.agni_api.core.entities.SpendingPeriodTemplate
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.spending_period_template.dto.GetSpendingPeriodTemplateBudgetOutput
import dev.auguste.agni_api.core.usecases.spending_period_template.dto.GetSpendingPeriodTemplateOutput
import java.util.UUID

class GetSpendingPeriodTemplate(
    private val spendingPeriodTemplateRepo: IRepository<SpendingPeriodTemplate>,
    private val budgetRepo: IRepository<Budget>,
): IUseCase<UUID, GetSpendingPeriodTemplateOutput>{
    override fun execAsync(input: UUID): GetSpendingPeriodTemplateOutput {
        val spendPeriodTemplate = spendingPeriodTemplateRepo.get(input) ?: throw DomainException.NotFound.SpendingPeriodTemplate(input)
        val budgets = budgetRepo.getManyByIds(spendPeriodTemplate.targetBudgetIds)
        return GetSpendingPeriodTemplateOutput(
            id = spendPeriodTemplate.id,
            recurrence = ScheduleRepeaterOutput(
                spendPeriodTemplate.recurrence.period.value,
                spendPeriodTemplate.recurrence.interval
            ),
            isActive = spendPeriodTemplate.checkIsActive(),
            startDate = spendPeriodTemplate.startDate,
            endDate = spendPeriodTemplate.endDate,
            budgets = budgets.map { it ->
                GetSpendingPeriodTemplateBudgetOutput(
                    id = it.id,
                    title = it.title
                )
            }
        )
    }
}