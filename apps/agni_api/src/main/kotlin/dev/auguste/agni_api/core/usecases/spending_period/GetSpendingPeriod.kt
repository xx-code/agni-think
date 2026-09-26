package dev.auguste.agni_api.core.usecases.spending_period

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.DomainException
import dev.auguste.agni_api.core.entities.SpendingPeriod
import dev.auguste.agni_api.core.entities.SpendingPeriodTemplate
import dev.auguste.agni_api.core.entities.enums.SpendingPeriodStateType
import dev.auguste.agni_api.core.usecases.budgets.dto.GetBudgetOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.spending_period.dto.ForcastSpendingPeriodInput
import dev.auguste.agni_api.core.usecases.spending_period.dto.ForcastSpendingPeriodOutput
import dev.auguste.agni_api.core.usecases.spending_period.dto.GetSpendingPeriodOutput
import dev.auguste.agni_api.core.usecases.spending_period.dto.SpendingPeriodItemOutput
import dev.auguste.agni_api.core.usecases.spending_period.dto.SpendingPeriodSnapShotOutput
import java.util.UUID

class GetSpendingPeriod(
    private val spendingPeriodRepo: IRepository<SpendingPeriod>,
    private val spendingPeriodTemplateRepo: IRepository<SpendingPeriodTemplate>,
    private val forcastSpendingPeriod: IUseCase<ForcastSpendingPeriodInput, ForcastSpendingPeriodOutput>,
): IUseCase<UUID, GetSpendingPeriodOutput> {
    override fun execAsync(input: UUID): GetSpendingPeriodOutput {
        val spendPeriod = spendingPeriodRepo.get(input) ?: throw DomainException.NotFound.SpendingPeriod(input)
        val spendingPeriodTemplate = spendingPeriodTemplateRepo.get(spendPeriod.spendingPeriodTemplateId) ?: throw DomainException.NotFound.SpendingPeriodTemplate(spendPeriod.spendingPeriodTemplateId)

        var forcast: ForcastSpendingPeriodOutput? = null
        if (spendPeriod.state != SpendingPeriodStateType.COMPLETE) {
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
