package dev.auguste.agni_api.core.usecases.budgets

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Budget
import dev.auguste.agni_api.core.entities.SavingGoal
import dev.auguste.agni_api.core.usecases.CreatedOutput
import dev.auguste.agni_api.core.usecases.budgets.dto.CreateBudgetInput
import dev.auguste.agni_api.core.entities.DomainException
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.value_objects.Scheduler
import dev.auguste.agni_api.core.value_objects.SchedulerRecurrence

class CreateBudget(
    private val budgetRepo: IRepository<Budget>,
    private val savingGoalRepo: IRepository<SavingGoal>
): IUseCase<CreateBudgetInput, CreatedOutput> {
    override fun execAsync(input: CreateBudgetInput): CreatedOutput {
        if (budgetRepo.existsByName(input.title))
            throw DomainException.AlreadyExist.Budget(input.title)

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