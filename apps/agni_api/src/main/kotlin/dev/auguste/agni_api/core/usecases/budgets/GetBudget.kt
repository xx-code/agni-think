package dev.auguste.agni_api.core.usecases.budgets

import dev.auguste.agni_api.core.adapters.dto.ScheduleRepeaterOutput
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Budget
import dev.auguste.agni_api.core.entities.SavingGoal
import dev.auguste.agni_api.core.entities.enums.InvoiceStatusType
import dev.auguste.agni_api.core.entities.enums.InvoiceType
import dev.auguste.agni_api.core.usecases.budgets.dto.GetBudgetOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.invoices.dto.GetBalanceInput
import dev.auguste.agni_api.core.usecases.invoices.dto.GetBalanceOutput
import dev.auguste.agni_api.core.usecases.invoices.dto.GetBalancesByPeriodInput
import java.util.UUID

class GetBudget(
    private val budgetRepo: IRepository<Budget>,
    private val savingGoalRepo: IRepository<SavingGoal>,
    private val getBalance: IUseCase<GetBalanceInput, GetBalanceOutput>
) : IUseCase<UUID, GetBudgetOutput> {
    override fun execAsync(input: UUID): GetBudgetOutput {
        val budget = budgetRepo.get(input) ?: throw Error("Saving goal id ${input}")

        var startDate = budget.scheduler.date
        if (budget.scheduler.repeater != null)
            startDate = budget.scheduler.downgradeDate()!! // verification of repeater in function

        val resultBalance = getBalance.execAsync(GetBalanceInput(
            budgetIds = setOf(budget.id),
            types = setOf(InvoiceType.FIXEDCOST, InvoiceType.VARIABLECOST, InvoiceType.OTHER),
            startDate = startDate,
            status = InvoiceStatusType.COMPLETED
        ))

        val currentBalance = resultBalance.balance

        val savingGoals = savingGoalRepo.getManyByIds(budget.targetSavingGoalIds)
        val saveBalance = savingGoals.sumOf { it.balance }

        return GetBudgetOutput(
            id = budget.id,
            title = budget.title,
            target = budget.target + saveBalance,
            realTarget = budget.target,
            savingGoalTarget = saveBalance,
            savingGoalIds = budget.targetSavingGoalIds,
            currentBalance = currentBalance,
            dueDate = budget.scheduler.date,
            repeater = budget.scheduler.repeater?.let {
                ScheduleRepeaterOutput(
                    it.period.value,
                    it.interval,
                )
            }
        )
    }
}