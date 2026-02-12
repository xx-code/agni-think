package dev.auguste.agni_api.core.usecases.budgets

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Budget
import dev.auguste.agni_api.core.entities.SavingGoal
import dev.auguste.agni_api.core.usecases.budgets.dto.UpdateBudgetInput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.value_objects.Scheduler
import dev.auguste.agni_api.core.value_objects.SchedulerRecurrence

class UpdateBudget(
    private val budgetRepo: IRepository<Budget>,
    private val savingGoalRepo: IRepository<SavingGoal>
): IUseCase<UpdateBudgetInput, Unit> {
    override fun execAsync(input: UpdateBudgetInput) {
        val budget = budgetRepo.get(input.id) ?: throw Error("Budget ID ${input.id} not found")

        if (input.title != null) {
            if (input.title != budget.title && budgetRepo.existsByName(input.title))
                throw Error("Budget title ${input.title} already exists")

            budget.title = input.title
        }

        if (input.target != null) {
            budget.target = input.target
        }

        if (!input.savingGoalIds.isNullOrEmpty()) {
            if (input.savingGoalIds != savingGoalRepo.getManyByIds(input.savingGoalIds))
                throw Error("Saving goal ids ${input.savingGoalIds} not found")

            budget.targetSavingGoalIds = input.savingGoalIds.toMutableSet()
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