package usecases.spending_period

import adapters.repositories.IRepository
import domain.exceptions.NotFoundException
import domain.entities.SpendingPeriod
import domain.entities.SpendingPeriodTemplate
import domain.enums.SpendingPeriodStateType
import usecases.budgets.dto.GetBudgetOutput
import usecases.interfaces.IUseCase
import usecases.spending_period.dto.ForcastSpendingPeriodInput
import usecases.spending_period.dto.ForcastSpendingPeriodOutput
import usecases.spending_period.dto.GetSpendingPeriodOutput
import usecases.spending_period.dto.SpendingPeriodItemOutput
import usecases.spending_period.dto.SpendingPeriodSnapShotOutput
import java.util.UUID

class GetSpendingPeriod(
    private val spendingPeriodRepo: IRepository<SpendingPeriod>,
    private val spendingPeriodTemplateRepo: IRepository<SpendingPeriodTemplate>,
    private val forcastSpendingPeriod: IUseCase<ForcastSpendingPeriodInput, ForcastSpendingPeriodOutput>,
): IUseCase<UUID, GetSpendingPeriodOutput> {
    override fun execAsync(input: UUID): GetSpendingPeriodOutput {
        val spendPeriod = spendingPeriodRepo.get(input) ?: throw NotFoundException.SingleEntity(input, "spending_period")
        val spendingPeriodTemplate = spendingPeriodTemplateRepo.get(spendPeriod.spendingPeriodTemplateId) ?: throw NotFoundException.SingleEntity(spendPeriod.spendingPeriodTemplateId, "spending_period_template")

        var forcast: ForcastSpendingPeriodOutput? = null
        if (spendPeriod.state != _root_ide_package_.domain.enums.SpendingPeriodStateType.COMPLETE) {
            forcast = forcastSpendingPeriod.execAsync(ForcastSpendingPeriodInput(
                startDate = spendPeriod.startDate,
                endDate = spendPeriod.endDate,
                budgetIds = spendingPeriodTemplate.targetBudgetIds.toList(),
                savingRate = spendPeriod.savingRateTarget
            ))
        }


        return GetSpendingPeriodOutput(
            id = spendPeriod.id,
            spendingPeriodTemplateId = spendPeriod.spendingPeriodTemplateId,
            startDate = spendPeriod.startDate,
            endDate = spendPeriod.endDate,
            freeAmount = spendPeriod.freeAmount,
            savingRateTarget = spendPeriod.savingRateTarget,
            totalExpectedIncome = spendPeriod.totalExpectedIncome,
            totalExpectedExpenses = spendPeriod.totalExpectedExpenses,
            state = spendPeriod.state,
            forcast = forcast,
            snapshot = SpendingPeriodSnapShotOutput(
                income = spendPeriod.snapshot.income,
                fixExpenses = spendPeriod.snapshot.fixExpenses,
                variableExpenses = spendPeriod.snapshot.variableExpenses,
                budgetExpenses = spendPeriod.snapshot.budgetExpenses,
                saving = spendPeriod.snapshot.saving
            ),
            wantSpendingItems = spendPeriod.wantSpendingItems.map {
                SpendingPeriodItemOutput(
                    description = it.description,
                    amount = it.amount
                )
            },
        )
    }
}
