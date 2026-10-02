package usecases.spending_period_template

import adapters.repositories.IRepository
import adapters.repositories.QueryExtendBuilder
import adapters.repositories.query_extend.QueryComparator
import domain.entities.Budget
import domain.exceptions.AlreadyExistException
import domain.exceptions.NotFoundException
import domain.entities.SpendingPeriodTemplate
import usecases.CreatedOutput
import usecases.interfaces.IUseCase
import usecases.spending_period_template.dto.CreateSpendingPeriodTemplateInput
import domain.value_objects.SchedulerRecurrence

class CreateSpendingPeriodTemplate(
    private val spendingPeriodTemplateRepo: IRepository<SpendingPeriodTemplate>,
    private val budgetRepo: IRepository<Budget>,
): IUseCase<CreateSpendingPeriodTemplateInput, CreatedOutput> {
    override fun execAsync(input: CreateSpendingPeriodTemplateInput): CreatedOutput {
        val conditionExistBuilder = QueryExtendBuilder<SpendingPeriodTemplate>()
            .addCondition("recurrence.period", QueryComparator.Equal, input.recurrence.period.value)
            .addCondition("recurrence.interval", QueryComparator.Equal, input.recurrence.interval)

        if (spendingPeriodTemplateRepo.exist(conditionExistBuilder))
            throw AlreadyExistException.EntitiesByField(mapOf("period" to input.recurrence.period, "interval" to input.recurrence.interval), "spending_period_template")

        if (input.targetBudgetIds.isNotEmpty() && budgetRepo.getManyByIds(input.targetBudgetIds).size != input.targetBudgetIds.size)
            throw NotFoundException.EntitiesByOtherField(mapOf("ids" to input.targetBudgetIds.joinToString()), "budget")

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