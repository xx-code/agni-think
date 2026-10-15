package usecases.spending_period

import usecases.interfaces.IUseCase

import usecases.UseCase
import adapters.IAuthenticateAdapter
import adapters.repositories.IRepository
import domain.exceptions.NotFoundException
import domain.entities.SpendingPeriod
import domain.entities.SpendingPeriodTemplate
import domain.enums.SpendingPeriodStateType
import usecases.spending_period.dto.ForcastSpendingPeriodInput
import usecases.spending_period.dto.ForcastSpendingPeriodOutput
import domain.value_objects.SnapshotForcastSpendingPeriod
import java.util.UUID

class CompleteSpendingPeriod(
    private val spendingPeriodRepo: IRepository<SpendingPeriod>,
    private val spendingPeriodTemplateRepo: IRepository<SpendingPeriodTemplate>,
    private val forcastSpendingPeriod: IUseCase<ForcastSpendingPeriodInput, ForcastSpendingPeriodOutput>,
    auth: IAuthenticateAdapter
): UseCase<UUID, Unit>(auth = auth) {
    override suspend fun process(input: UUID) {
        val spendPeriod = spendingPeriodRepo.get(input) ?: throw NotFoundException.SingleEntity(input, "spending_period")
        val spendingPeriodTemplate = spendingPeriodTemplateRepo.get(spendPeriod.spendingPeriodTemplateId) ?: throw NotFoundException.SingleEntity(spendPeriod.spendingPeriodTemplateId, "spending_period_template")

        spendPeriod.state = SpendingPeriodStateType.COMPLETE

        val forcast = forcastSpendingPeriod.processDirect(ForcastSpendingPeriodInput(
            userId = spendPeriod.userId,
            startDate = spendPeriod.startDate,
            endDate = spendPeriod.endDate,
            budgetIds = spendingPeriodTemplate.targetBudgetIds,
            savingRate = spendPeriod.savingRateTarget,
            scheduleInvoiceIds = spendPeriod.scheduleIncomesSelected + spendPeriod.scheduleExpensesSelected,
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
