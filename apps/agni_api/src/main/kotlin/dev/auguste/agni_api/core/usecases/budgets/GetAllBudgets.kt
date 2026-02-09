package dev.auguste.agni_api.core.usecases.budgets

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.dto.ScheduleRepeaterOutput
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Budget
import dev.auguste.agni_api.core.entities.SavingGoal
import dev.auguste.agni_api.core.entities.enums.InvoiceStatusType
import dev.auguste.agni_api.core.entities.enums.InvoiceType
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.budgets.dto.GetBudgetOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.invoices.dto.GetBalanceInput
import dev.auguste.agni_api.core.usecases.invoices.dto.GetBalanceOutput

class GetAllBudgets(
    private val budgetRepo: IRepository<Budget>,
    private val savingGoalRepo: IRepository<SavingGoal>,
    private val getBalance: IUseCase<GetBalanceInput, GetBalanceOutput>
) : IUseCase<QueryFilter, ListOutput<GetBudgetOutput>> {
    override fun execAsync(input: QueryFilter): ListOutput<GetBudgetOutput> {
        val budgets = budgetRepo.getAll(query = input)
        val savingGoals = savingGoalRepo.getManyByIds(budgets.items.flatMap { it.targetSavingGoalIds }.toSet())

        val result = mutableListOf<GetBudgetOutput>()
        for (budget in budgets.items) {
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
            val saveBalance = savingGoals.filter { budget.targetSavingGoalIds.contains(it.id) }.sumOf { it.balance }

            result.add(
                GetBudgetOutput(
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
                            it.period,
                            it.interval,
                        )
                    }
                )
            )
        }

        return ListOutput(result, budgets.total)
    }
}