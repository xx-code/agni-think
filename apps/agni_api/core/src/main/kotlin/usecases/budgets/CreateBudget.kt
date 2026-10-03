package usecases.budgets

import adapters.repositories.IRepository
import domain.entities.Budget
import domain.entities.Fund
import usecases.CreatedOutput
import usecases.budgets.dto.CreateBudgetInput
import domain.exceptions.AlreadyExistException
import usecases.interfaces.IUseCase
import domain.value_objects.Scheduler
import domain.value_objects.SchedulerRecurrence

class CreateBudget(
    private val budgetRepo: IRepository<Budget>
): IUseCase<CreateBudgetInput, CreatedOutput> {
    override fun execAsync(input: CreateBudgetInput): CreatedOutput {
        if (budgetRepo.existsByName(input.title))
            throw AlreadyExistException.EntitiesByField(mapOf("name" to input.title), "budget")

        val newBudget = Budget(
            title = input.title,
            target = input.target,
            scheduler = Scheduler(
                date = input.schedule.dueDate,
                repeater = input.schedule.repeater?.let {
                    SchedulerRecurrence(
                        it.period,
                        it.interval,
                    )
                }
            )
        )

        budgetRepo.create(newBudget)

        return CreatedOutput(newBudget.id)
    }
}