package dev.auguste.agni_api.core.usecases.spending_period

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.DomainException
import dev.auguste.agni_api.core.entities.SpendingPeriod
import dev.auguste.agni_api.core.entities.SpendingPeriodTemplate
import dev.auguste.agni_api.core.usecases.CreatedOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.spending_period.dto.CreateSpendingPeriodInput
import dev.auguste.agni_api.core.value_objects.Scheduler
import dev.auguste.agni_api.core.value_objects.SnapshotForcastSpendingPeriod
import dev.auguste.agni_api.core.value_objects.SpendingPeriodItem
import kotlin.collections.map

class CreateSpendingPeriod(
    private val spendingPeriodRepo: IRepository<SpendingPeriod>,
    private val spendingPeriodTemplateRepo: IRepository<SpendingPeriodTemplate>,
): IUseCase<CreateSpendingPeriodInput, CreatedOutput> {
    override fun execAsync(input: CreateSpendingPeriodInput): CreatedOutput {
        val template = spendingPeriodTemplateRepo.get(input.spendingPeriodTemplateId) ?: throw DomainException.NotFound.SpendingPeriodTemplate(input.spendingPeriodTemplateId)

        val scheduler = Scheduler(
            template.startDate.atStartOfDay(),
            template.recurrence
        )

        val newSpendingPeriod = SpendingPeriod(
            spendingPeriodTemplateId = input.spendingPeriodTemplateId,
            startDate = template.startDate,
            endDate = scheduler.upgradeDate().toLocalDate(),
            freeAmount = input.freeAmount,
            savingRateTarget = input.savingRateTarget,
            totalExpectedIncome = input.totalExpectedIncome,
            totalExpectedExpenses = input.totalExpectedExpenses,
            state = input.state,
            wantSpendingItems = input.wantSpendingItems.map {
                SpendingPeriodItem(
                    description = it.description,
                    amount = it.amount
                )
            },
            snapshot = SnapshotForcastSpendingPeriod(
                income = 0.0,
                fixExpenses = 0.0,
                variableExpenses = 0.0,
                budgetExpenses = 0.0,
                saving = 0.0
            )
        )

        spendingPeriodRepo.create(newSpendingPeriod)

        return CreatedOutput(newSpendingPeriod.id)
    }
}
