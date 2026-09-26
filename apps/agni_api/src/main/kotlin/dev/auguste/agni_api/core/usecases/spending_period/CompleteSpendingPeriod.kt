package dev.auguste.agni_api.core.usecases.spending_period

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.DomainException
import dev.auguste.agni_api.core.entities.SpendingPeriod
import dev.auguste.agni_api.core.entities.SpendingPeriodTemplate
import dev.auguste.agni_api.core.entities.enums.SpendingPeriodStateType
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.spending_period.dto.ForcastSpendingPeriodInput
import dev.auguste.agni_api.core.usecases.spending_period.dto.ForcastSpendingPeriodOutput
import dev.auguste.agni_api.core.usecases.spending_period.dto.UpdateSpendingPeriodInput
import dev.auguste.agni_api.core.value_objects.SnapshotForcastSpendingPeriod
import dev.auguste.agni_api.core.value_objects.SpendingPeriodItem
import java.util.UUID

class CompleteSpendingPeriod(
    private val spendingPeriodRepo: IRepository<SpendingPeriod>,
    private val spendingPeriodTemplateRepo: IRepository<SpendingPeriodTemplate>,
    private val forcastSpendingPeriod: IUseCase<ForcastSpendingPeriodInput, ForcastSpendingPeriodOutput>,
): IUseCase<UUID, Unit> {
    override fun execAsync(input: UUID) {
        val spendPeriod = spendingPeriodRepo.get(input) ?: throw DomainException.NotFound.SpendingPeriod(input)
        val spendingPeriodTemplate = spendingPeriodTemplateRepo.get(spendPeriod.spendingPeriodTemplateId) ?: throw DomainException.NotFound.SpendingPeriodTemplate(spendPeriod.spendingPeriodTemplateId)

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
