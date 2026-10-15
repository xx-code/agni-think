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
import usecases.spending_period.dto.GetSpendingPeriodOutput
import usecases.spending_period.dto.SpendingPeriodItemOutput
import usecases.spending_period.dto.SpendingPeriodSnapShotOutput
import java.util.UUID

class GetSpendingPeriod(
    private val spendingPeriodRepo: IRepository<SpendingPeriod>,
    private val forcastSpendingPeriod: IUseCase<ForcastSpendingPeriodInput, ForcastSpendingPeriodOutput>,
    auth: IAuthenticateAdapter
): UseCase<UUID, GetSpendingPeriodOutput>(auth = auth) {
    override suspend fun process(input: UUID): GetSpendingPeriodOutput {
        val spendPeriod = spendingPeriodRepo.get(input) ?: throw NotFoundException.SingleEntity(input, "spending_period")

        var forcast: ForcastSpendingPeriodOutput? = null
        if (spendPeriod.state != SpendingPeriodStateType.COMPLETE) {
            forcast = forcastSpendingPeriod.processDirect(ForcastSpendingPeriodInput(
                userId = spendPeriod.userId,
                startDate = spendPeriod.startDate,
                endDate = spendPeriod.endDate,
                budgetIds = spendPeriod.budgetsSelected,
                savingRate = spendPeriod.savingRateTarget,
                scheduleInvoiceIds = spendPeriod.scheduleIncomesSelected + spendPeriod.scheduleExpensesSelected
            ))
        }


        return GetSpendingPeriodOutput(
            id = spendPeriod.id,
            spendingPeriodTemplateId = spendPeriod.spendingPeriodTemplateId,
            startDate = spendPeriod.startDate,
            endDate = spendPeriod.endDate,
            freeAmount = forcast?.expectedRemainAmount ?: spendPeriod.closedBalance,
            savingRateTarget = spendPeriod.savingRateTarget,
            totalExpectedIncome = forcast?.totalExpectedIncome ?: spendPeriod.snapshot.income,
            totalExpectedExpenses = forcast?.totalExpectedExpense
                ?: (spendPeriod.snapshot.variableExpenses + spendPeriod.snapshot.fixExpenses),
            state = spendPeriod.state,
            forcast = forcast,
            snapshot = SpendingPeriodSnapShotOutput(
                income = spendPeriod.snapshot.income,
                fixExpenses = spendPeriod.snapshot.fixExpenses,
                variableExpenses = spendPeriod.snapshot.variableExpenses,
                budgetExpenses = spendPeriod.snapshot.budgetExpenses,
                saving = spendPeriod.snapshot.saving
            ),
            wishlistItems = setOf(),
            scheduleIncomesSelected = spendPeriod.scheduleIncomesSelected,
            scheduleExpensesSelected = spendPeriod.scheduleExpensesSelected,
            budgetsSelected = spendPeriod.budgetsSelected
        )
    }
}
