package usecases.budgets

import adapters.dto.ScheduleRepeaterOutput
import adapters.repositories.IRepository
import domain.entities.Budget
import domain.exceptions.NotFoundException
import domain.enums.InvoiceType
import usecases.budgets.dto.GetBudgetOutput
import usecases.interfaces.IUseCase
import usecases.invoices.dto.GetBalanceInput
import usecases.invoices.dto.GetBalanceOutput
import java.util.UUID

class GetBudget(
    private val budgetRepo: IRepository<Budget>,
    private val getBalance: IUseCase<GetBalanceInput, GetBalanceOutput>
) : IUseCase<UUID, GetBudgetOutput> {
    override fun execAsync(input: UUID): GetBudgetOutput {
        val budget = budgetRepo.get(input) ?: throw NotFoundException.SingleEntity(input, "budget")

        val startDate = budget.scheduler.downgradeDate()
        val endDate = budget.scheduler.upgradeDate()

        val resultBalance = getBalance.execAsync(GetBalanceInput(
            budgetIds = setOf(budget.id),
            types = setOf(_root_ide_package_.domain.enums.InvoiceType.FIXED_COST, _root_ide_package_.domain.enums.InvoiceType.VARIABLE_COST, _root_ide_package_.domain.enums.InvoiceType.OTHER),
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