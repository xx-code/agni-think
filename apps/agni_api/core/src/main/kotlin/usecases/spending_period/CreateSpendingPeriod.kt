package usecases.spending_period

import usecases.UseCase
import adapters.repositories.IRepository
import domain.exceptions.NotFoundException
import domain.entities.SpendingPeriod
import domain.entities.SpendingPeriodTemplate
import usecases.dto.CreatedOutput
import usecases.spending_period.dto.CreateSpendingPeriodInput
import domain.value_objects.Scheduler
import domain.value_objects.SnapshotForcastSpendingPeriod
import domain.value_objects.SpendingPeriodItem
import kotlin.collections.map

class CreateSpendingPeriod(
    private val spendingPeriodRepo: IRepository<SpendingPeriod>,
    private val spendingPeriodTemplateRepo: IRepository<SpendingPeriodTemplate>,
): UseCase<CreateSpendingPeriodInput, CreatedOutput>() {
    override suspend fun process(input: CreateSpendingPeriodInput): CreatedOutput {
        val template = spendingPeriodTemplateRepo.get(input.spendingPeriodTemplateId) ?: throw NotFoundException.SingleEntity(input.spendingPeriodTemplateId, "spending_period_template")

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
