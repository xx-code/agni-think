package usecases.budgets

import adapters.IAuthenticateAdapter
import usecases.UseCase
import adapters.repositories.IRepository
import adapters.repositories.QueryComparator
import adapters.repositories.QueryExtendBuilder
import domain.entities.Budget
import domain.entities.User
import usecases.dto.CreatedOutput
import usecases.budgets.dto.CreateBudgetInput
import domain.exceptions.AlreadyExistException
import domain.value_objects.Scheduler
import domain.value_objects.SchedulerRecurrence

class CreateBudget(
    private val budgetRepo: IRepository<Budget>,
    auth: IAuthenticateAdapter
): UseCase<CreateBudgetInput, CreatedOutput>(auth = auth) {
    override suspend fun process(input: CreateBudgetInput): CreatedOutput {
        val currentUser = getCurrentUser()

        val condition = QueryExtendBuilder<Budget>()
            .addCondition("title", QueryComparator.Equal, input.title)
        if (budgetRepo.exist(condition))
            throw AlreadyExistException.EntitiesByField(mapOf("name" to input.title), "budget")

        val newBudget = Budget(
            userId = currentUser.id,
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