package dev.auguste.agni_api.core.usecases.spending_period_template

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.adapters.repositories.QueryExtendBuilder
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryComparator
import dev.auguste.agni_api.core.entities.Budget
import dev.auguste.agni_api.core.entities.DomainException
import dev.auguste.agni_api.core.entities.SpendingPeriodTemplate
import dev.auguste.agni_api.core.usecases.CreatedOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.spending_period_template.dto.CreateSpendingPeriodTemplateInput
import dev.auguste.agni_api.core.value_objects.SchedulerRecurrence
import java.time.LocalDate

class CreateSpendingPeriodTemplate(
    private val spendingPeriodTemplateRepo: IRepository<SpendingPeriodTemplate>,
    private val budgetRepo: IRepository<Budget>,
): IUseCase<CreateSpendingPeriodTemplateInput, CreatedOutput> {
    override fun execAsync(input: CreateSpendingPeriodTemplateInput): CreatedOutput {
        val conditionExistBuilder = QueryExtendBuilder<SpendingPeriodTemplate>()
            .addCondition("recurrence.period", QueryComparator.Equal, input.recurrence.period.value)
            .addCondition("recurrence.interval", QueryComparator.Equal, input.recurrence.interval)

        if (spendingPeriodTemplateRepo.exist(conditionExistBuilder))
            throw DomainException.AlreadyExist.SpendingPeriodTemplateExist(input.recurrence.period, input.recurrence.interval)

        if (budgetRepo.getManyByIds(input.targetBudgetIds).isEmpty())
            throw DomainException.NotFound.SomeBudgets(input.targetBudgetIds)

        val newSpendingPeriodTemplate = SpendingPeriodTemplate(
            startDate = input.startDate,
            recurrence = SchedulerRecurrence(
                period = input.recurrence.period,
                interval = input.recurrence.interval
            ),
            targetBudgetIds = input.targetBudgetIds,
            isActive = false,
            endDate = input.endDate
        )

        spendingPeriodTemplateRepo.create(newSpendingPeriodTemplate)

        return CreatedOutput(newSpendingPeriodTemplate.id)
    }
}