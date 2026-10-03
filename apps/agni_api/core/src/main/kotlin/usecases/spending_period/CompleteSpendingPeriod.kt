package usecases.spending_period

import adapters.repositories.IRepository
import domain.exceptions.NotFoundException
import domain.entities.SpendingPeriod
import domain.entities.SpendingPeriodTemplate
import domain.enums.SpendingPeriodStateType
import usecases.interfaces.IUseCase
import usecases.spending_period.dto.ForcastSpendingPeriodInput
import usecases.spending_period.dto.ForcastSpendingPeriodOutput
import domain.value_objects.SnapshotForcastSpendingPeriod
import java.util.UUID

class CompleteSpendingPeriod(
    private val spendingPeriodRepo: IRepository<SpendingPeriod>,
    private val spendingPeriodTemplateRepo: IRepository<SpendingPeriodTemplate>,
    private val forcastSpendingPeriod: IUseCase<ForcastSpendingPeriodInput, ForcastSpendingPeriodOutput>,
): IUseCase<UUID, Unit> {
    override fun execAsync(input: UUID) {
        val spendPeriod = spendingPeriodRepo.get(input) ?: throw NotFoundException.SingleEntity(input, "spending_period")
        val spendingPeriodTemplate = spendingPeriodTemplateRepo.get(spendPeriod.spendingPeriodTemplateId) ?: throw NotFoundException.SingleEntity(spendPeriod.spendingPeriodTemplateId, "spending_period_template")

        spendPeriod.state = SpendingPeriodStateType.COMPLETE

        val forcast = forcastSpendingPeriod.execAsync(ForcastSpendingPeriodInput(
            startDate = spendPeriod.startDate,
            endDate = spendPeriod.endDate,
            budgetIds = spendingPeriodTemplate.targetBudgetIds.toList(),
            savingRate = spendPeriod.savingRateTarget
        ))

        spendPeriod.closedBalance = forcast.currentRemainAmount
        spendPeriod.snapshot = SnapshotForcastSpendingPeriod(
            income = forcast.currentIncome,
            fixExpenses = forcast.fixExpenseItems.sumOf { it.validAmount },
            variableExpenses = forcast.variableExpenseItems.sumOf { it.validAmount },
            budgetExpenses = forcast.currentBudgetExpense,
            saving = forcast.currentSaving
        )

        if (spendPeriod.hasChanged())
            spendingPeriodRepo.update(spendPeriod)
    }
}
