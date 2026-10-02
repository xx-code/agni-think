package usecases.budgets

import adapters.repositories.IRepository
import domain.entities.Budget
import domain.exceptions.AlreadyExistException
import domain.exceptions.NotFoundException
import usecases.budgets.dto.UpdateBudgetInput
import usecases.interfaces.IUseCase
import domain.value_objects.Scheduler
import domain.value_objects.SchedulerRecurrence

class UpdateBudget(
    private val budgetRepo: IRepository<Budget>
): IUseCase<UpdateBudgetInput, Unit> {
    override fun execAsync(input: UpdateBudgetInput) {
        val budget = budgetRepo.get(input.id) ?: throw NotFoundException.SingleEntity(input.id, "budget")

        if (input.title != null) {
            if (input.title != budget.title && budgetRepo.existsByName(input.title))
                throw AlreadyExistException.EntitiesByField(mapOf("name" to input.title), "budget")

            budget.title = input.title
        }

        if (input.target != null) {
            budget.target = input.target
        }

        if (input.schedule != null) {
            budget.scheduler = Scheduler(
                input.schedule.dueDate,
                repeater = input.schedule.repeater?.let {
                    SchedulerRecurrence(
                        period = it.period,
                        interval = it.interval
                    )
                }
            )
        }

        if (budget.hasChanged())
            budgetRepo.update(budget)
    }
}