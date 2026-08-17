package dev.auguste.agni_api.core.usecases.analystics

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Budget
import dev.auguste.agni_api.core.entities.enums.InvoiceType
import dev.auguste.agni_api.core.usecases.analystics.dto.GetBudgetTotalSummaryOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.invoices.dto.GetBalanceInput
import dev.auguste.agni_api.core.usecases.invoices.dto.GetBalanceOutput

class GetBudgetTotalSummary(
    private val repoBudget: IRepository<Budget>,
    private val getBalance: IUseCase<GetBalanceInput, GetBalanceOutput>
): IUseCase<Unit, GetBudgetTotalSummaryOutput> {
    override fun execAsync(input: Unit): GetBudgetTotalSummaryOutput {
        val budgets = repoBudget.getAll(QueryFilter.queryAll())
        val budgetRepeat = budgets.items.filter { it.scheduler.repeater != null}
        val budgetNoRepeat = budgets.items.filter { it.scheduler.repeater == null}

        val idsNoRepeat = budgetNoRepeat.map { it.id }
        val idsRepeat = budgetRepeat.map { it.id }

        val totalBudget = budgets.items.sumOf { it.target }

        val minStartDateRepeat = budgetRepeat
            .mapNotNull { it.scheduler.downgradeDate() }
            .minOrNull()
        val maxEndDateRepeat = budgetRepeat
            .maxByOrNull { it.scheduler.upgradeDate() }
            ?.scheduler?.upgradeDate()

        val minStartDateNoRepeat = budgetNoRepeat
            .mapNotNull { it.scheduler.downgradeDate() }
            .minOrNull()
        val maxEndDateNoRepeat = budgetNoRepeat
            .maxByOrNull { it.scheduler.upgradeDate() }
            ?.scheduler?.upgradeDate()

        val balanceRepeatOut = getBalance.execAsync(GetBalanceInput(
            budgetIds = idsRepeat.toSet(),
            types = setOf(InvoiceType.FIXEDCOST, InvoiceType.VARIABLECOST, InvoiceType.OTHER),
            startDate = minStartDateRepeat,
            endDate = maxEndDateRepeat,
        ))

        val balanceNoRepeatOut = getBalance.execAsync(GetBalanceInput(
            budgetIds = idsNoRepeat.toSet(),
            types = setOf(InvoiceType.FIXEDCOST, InvoiceType.VARIABLECOST, InvoiceType.OTHER),
            startDate = minStartDateNoRepeat,
            endDate = maxEndDateNoRepeat
        ))

        return GetBudgetTotalSummaryOutput(
            totalBudget = totalBudget.toLong(),
            totalSpend = (balanceRepeatOut.spend + balanceNoRepeatOut.spend).toLong()
        )
    }
}