package dev.auguste.agni_api.core.usecases.budgets

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Budget
import dev.auguste.agni_api.core.entities.DomainException
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
        val budget = budgetRepo.get(input.id) ?: throw DomainException.NotFound.Budget(input.id)

        if (input.title != null) {
            if (input.title != budget.title && budgetRepo.existsByName(input.title))
                throw DomainException.AlreadyExist.Budget(input.title)

            budget.title = input.title
        }

        if (input.target != null) {
            budget.target = input.target
        }

        if (!input.savingGoalIds.isNullOrEmpty()) {
            if (input.savingGoalIds != savingGoalRepo.getManyByIds(input.savingGoalIds))
                throw DomainException.NotFound.SomeSavingGoals(input.savingGoalIds)

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