package dev.auguste.agni_api.core.usecases.budgets

import dev.auguste.agni_api.core.adapters.dto.ScheduleRepeaterOutput
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Budget
import dev.auguste.agni_api.core.entities.DomainException
import dev.auguste.agni_api.core.entities.enums.InvoiceType
import dev.auguste.agni_api.core.usecases.budgets.dto.GetBudgetOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.invoices.dto.GetBalanceInput
import dev.auguste.agni_api.core.usecases.invoices.dto.GetBalanceOutput
import java.util.UUID

class GetBudget(
    private val budgetRepo: IRepository<Budget>,
    private val getBalance: IUseCase<GetBalanceInput, GetBalanceOutput>
) : IUseCase<UUID, GetBudgetOutput> {
    override fun execAsync(input: UUID): GetBudgetOutput {
        val budget = budgetRepo.get(input) ?: throw DomainException.NotFound.Budget(input)

        val startDate = budget.scheduler.downgradeDate()
        val endDate = budget.scheduler.upgradeDate()

        val resultBalance = getBalance.execAsync(GetBalanceInput(
            budgetIds = setOf(budget.id),
            types = setOf(InvoiceType.FIXEDCOST, InvoiceType.VARIABLECOST, InvoiceType.OTHER),
            startDate = startDate,
            endDate = endDate
        ))

        val currentBalance = resultBalance.balance

        return GetBudgetOutput(
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
    }
}