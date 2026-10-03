package usecases.budgets

import usecases.UseCase
import adapters.repositories.IRepository
import domain.entities.Budget
import usecases.dto.CreatedOutput
import usecases.budgets.dto.CreateBudgetInput
import domain.exceptions.AlreadyExistException
import domain.value_objects.Scheduler
import domain.value_objects.SchedulerRecurrence

class CreateBudget(
    private val budgetRepo: IRepository<Budget>
): UseCase<CreateBudgetInput, CreatedOutput>() {
    override suspend fun process(input: CreateBudgetInput): CreatedOutput {
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