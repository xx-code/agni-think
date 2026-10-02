package usecases.spending_period_template

import adapters.repositories.IRepository
import adapters.repositories.QueryExtendBuilder
import adapters.repositories.query_extend.QueryComparator
import domain.entities.Budget
import domain.exceptions.AlreadyExistException
import domain.exceptions.NotFoundException
import domain.entities.SpendingPeriodTemplate
import usecases.interfaces.IUseCase
import usecases.spending_period_template.dto.UpdateSpendingPeriodTemplateInput
import domain.value_objects.SchedulerRecurrence

class UpdateSpendingPeriodTemplate(
    private val spendingPeriodTemplateRepo: IRepository<SpendingPeriodTemplate>,
    private val budgetRepo: IRepository<Budget>,
): IUseCase<UpdateSpendingPeriodTemplateInput, Unit> {
    override fun execAsync(input: UpdateSpendingPeriodTemplateInput) {
        val spendPeriodTemplate = spendingPeriodTemplateRepo.get(input.id) ?: throw NotFoundException.SingleEntity(input.id, "spending_period_template")

        if (input.recurrence != null) {
            val conditionExistBuilder = QueryExtendBuilder<SpendingPeriodTemplate>()
                .addCondition("recurrence.period", QueryComparator.Equal, input.recurrence.period.value)
                .addCondition("recurrence.interval", QueryComparator.Equal, input.recurrence.interval)

            if (spendPeriodTemplate.recurrence.period != input.recurrence.period &&
                spendPeriodTemplate.recurrence.interval != input.recurrence.interval &&
                spendingPeriodTemplateRepo.exist(conditionExistBuilder))
                throw AlreadyExistException.EntitiesByField(mapOf("period" to input.recurrence.period, "interval" to input.recurrence.interval), "spending_period_template")

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

        if (input.targetBudgetIds != null) {
            if (input.targetBudgetIds.isNotEmpty() && budgetRepo.getManyByIds(input.targetBudgetIds).size != input.targetBudgetIds.size)
                throw NotFoundException.EntitiesByOtherField(mapOf("ids" to input.targetBudgetIds.joinToString()), "budget")

            spendPeriodTemplate.targetBudgetIds = input.targetBudgetIds
        }

        if (input.isActive != null) {
            val condBuilder = QueryExtendBuilder<SpendingPeriodTemplate>().addCondition("isActive", QueryComparator.Equal, input.isActive)
            if (!spendPeriodTemplate.isActive && input.isActive && spendingPeriodTemplateRepo.exist(condBuilder))
                throw AlreadyExistException.Entity("spending_period")

            spendPeriodTemplate.isActive = input.isActive
        }

        if (spendPeriodTemplate.hasChanged())
            spendingPeriodTemplateRepo.update(spendPeriodTemplate)
    }
}