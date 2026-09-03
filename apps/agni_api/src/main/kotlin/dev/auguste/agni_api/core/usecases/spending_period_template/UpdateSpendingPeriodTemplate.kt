package dev.auguste.agni_api.core.usecases.spending_period_template

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.adapters.repositories.QueryExtendBuilder
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryComparator
import dev.auguste.agni_api.core.entities.Budget
import dev.auguste.agni_api.core.entities.DomainException
import dev.auguste.agni_api.core.entities.SpendingPeriodTemplate
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.spending_period_template.dto.UpdateSpendingPeriodTemplateInput
import dev.auguste.agni_api.core.value_objects.SchedulerRecurrence

class UpdateSpendingPeriodTemplate(
    private val spendingPeriodTemplateRepo: IRepository<SpendingPeriodTemplate>,
    private val budgetRepo: IRepository<Budget>,
): IUseCase<UpdateSpendingPeriodTemplateInput, Unit> {
    override fun execAsync(input: UpdateSpendingPeriodTemplateInput) {
        val spendPeriodTemplate = spendingPeriodTemplateRepo.get(input.id) ?: throw DomainException.NotFound.SpendingPeriodTemplate(input.id)

        if (input.recurrence != null) {
            val conditionExistBuilder = QueryExtendBuilder<SpendingPeriodTemplate>()
                .addCondition("recurrence.period", QueryComparator.Equal, input.recurrence.period)
                .addCondition("recurrence.interval", QueryComparator.Equal, input.recurrence.interval)

            if (spendingPeriodTemplateRepo.exist(conditionExistBuilder))
                throw DomainException.AlreadyExist.SpendingPeriodTemplateExist(input.recurrence.period, input.recurrence.interval)

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
            if (budgetRepo.getManyByIds(input.targetBudgetIds).isEmpty())
                throw DomainException.NotFound.SomeBudgets(input.targetBudgetIds)
            spendPeriodTemplate.targetBudgetIds = input.targetBudgetIds
        }

        if (input.isActive != null) {
            val condBuilder = QueryExtendBuilder<SpendingPeriodTemplate>().addCondition("isActive", QueryComparator.Equal, input.isActive)
            if (input.isActive && spendingPeriodTemplateRepo.exist(condBuilder))
                throw DomainException.AlreadyExist.SpendingPeriodTemplateAlreadyActive()

            spendPeriodTemplate.isActive = input.isActive
        }

        if (spendPeriodTemplate.hasChanged())
            spendingPeriodTemplateRepo.update(spendPeriodTemplate)
    }
}