package dev.auguste.agni_api.core.usecases.budgets

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.dto.QuerySortBy
import dev.auguste.agni_api.core.adapters.dto.ScheduleRepeaterOutput
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryBudgetExtend
import dev.auguste.agni_api.core.entities.Budget
import dev.auguste.agni_api.core.entities.enums.InvoiceType
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.budgets.dto.GetAllBudgetInput
import dev.auguste.agni_api.core.usecases.budgets.dto.GetBudgetOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.invoices.dto.GetBalanceInput
import dev.auguste.agni_api.core.usecases.invoices.dto.GetBalanceOutput

class GetAllBudgets(
    private val budgetRepo: IRepository<Budget>,
    private val getBalance: IUseCase<GetBalanceInput, GetBalanceOutput>
) : IUseCase<GetAllBudgetInput, ListOutput<GetBudgetOutput>> {
    override fun execAsync(input: GetAllBudgetInput): ListOutput<GetBudgetOutput> {
        val query = QueryFilter(
            offset = input.query.offset,
            limit = input.query.limit,
            queryAll = input.query.queryAll,
            sortBy = QuerySortBy("updated_at")
        )
        val budgets = budgetRepo.getAll(
            query = query,
            QueryBudgetExtend(periodTypes = input.periodTypes))

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