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
    private val getBalance: IUseCase<GetBalanceInput, GetBalanceOutput>
) : IUseCase<QueryFilter, ListOutput<GetBudgetOutput>> {
    override fun execAsync(input: QueryFilter): ListOutput<GetBudgetOutput> {
        val budgets = budgetRepo.getAll(query = input)

        val result = mutableListOf<GetBudgetOutput>()
        for (budget in budgets.items) {
            val startDate = budget.scheduler.downgradeDate()
            val endDate = budget.scheduler.upgradeDate()

            val resultBalance = getBalance.execAsync(GetBalanceInput(
                budgetIds = setOf(budget.id),
                types = setOf(InvoiceType.FIXEDCOST, InvoiceType.VARIABLECOST, InvoiceType.OTHER),
                startDate = startDate,
                endDate = endDate
            ))

            val currentBalance = resultBalance.spend

            result.add(
                GetBudgetOutput(
                    id = budget.id,
                    title = budget.title,
                    target = budget.target,
                    currentBalance = currentBalance,
                    dueDate = budget.scheduler.date,
                    repeater = budget.scheduler.repeater?.let {
                        ScheduleRepeaterOutput(
                            it.period.value,
                            it.interval,
                        )
                    }
                )
            )
        }

        return ListOutput(result, budgets.total)
    }
}