package usecases.budgets

import adapters.dto.QueryFilter
import adapters.dto.QuerySortBy
import adapters.dto.ScheduleRepeaterOutput
import adapters.repositories.IRepository
import adapters.repositories.query_extend.QueryBudgetExtend
import domain.entities.Budget
import domain.enums.InvoiceType
import usecases.ListOutput
import usecases.budgets.dto.GetAllBudgetInput
import usecases.budgets.dto.GetBudgetOutput
import usecases.interfaces.IUseCase
import usecases.invoices.dto.GetBalanceInput
import usecases.invoices.dto.GetBalanceOutput

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
                types = setOf(_root_ide_package_.domain.enums.InvoiceType.FIXED_COST, _root_ide_package_.domain.enums.InvoiceType.VARIABLE_COST, _root_ide_package_.domain.enums.InvoiceType.OTHER),
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