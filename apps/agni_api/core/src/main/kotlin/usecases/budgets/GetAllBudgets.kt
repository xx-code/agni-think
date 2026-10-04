package usecases.budgets

import usecases.UseCase
import adapters.dto.QueryFilter
import adapters.dto.QuerySortBy
import adapters.dto.ScheduleRepeaterOutput
import adapters.repositories.IRepository
import adapters.repositories.QueryExtendBuilder
import adapters.repositories.QueryComparator
import domain.entities.Budget
import domain.enums.InvoiceType
import usecases.dto.ListOutput
import usecases.budgets.dto.GetAllBudgetInput
import usecases.budgets.dto.GetBudgetOutput
import usecases.interfaces.IUseCase
import usecases.invoices.dto.GetBalanceInput
import usecases.invoices.dto.GetBalanceOutput

class GetAllBudgets(
    private val budgetRepo: IRepository<Budget>,
    private val getBalance: IUseCase<GetBalanceInput, GetBalanceOutput>
): UseCase<GetAllBudgetInput, ListOutput<GetBudgetOutput>>() {
    override suspend fun process(input: GetAllBudgetInput): ListOutput<GetBudgetOutput> {
        val query = QueryFilter(
            offset = input.query.offset,
            limit = input.query.limit,
            queryAll = input.query.queryAll,
            sortBy = QuerySortBy("updated_at")
        )

        val conditionBudget = QueryExtendBuilder<Budget>()
        if (input.periodTypes != null)
            conditionBudget.addCondition("scheduler.repeater", QueryComparator.In, input.periodTypes.map { it.value }.toSet())

        val budgets = budgetRepo.getAll(
            query = query,
            conditionBudget
        )

        val result = mutableListOf<GetBudgetOutput>()
        for (budget in budgets.items) {
            if (!input.loadBalance)
                result.add(
                    GetBudgetOutput(
                        id = budget.id,
                        title = budget.title,
                        target = budget.target,
                        currentBalance = 0.0,
                        dueDate = budget.scheduler.date,
                        repeater = budget.scheduler.repeater?.let {
                            ScheduleRepeaterOutput(
                                it.period.value,
                                it.interval,
                            )
                        }
                    )
                )

            val startDate = budget.scheduler.downgradeDate()
            val endDate = budget.scheduler.upgradeDate()

            val resultBalance = getBalance.processDirect(GetBalanceInput(
                budgetIds = setOf(budget.id),
                types = setOf(InvoiceType.FIXED_COST, InvoiceType.VARIABLE_COST, InvoiceType.OTHER),
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