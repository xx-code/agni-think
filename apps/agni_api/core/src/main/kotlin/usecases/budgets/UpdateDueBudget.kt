package usecases.budgets

import adapters.dto.QueryFilter
import adapters.events.EventType
import adapters.events.IEventRegister
import adapters.events.contents.NotificationEventContent
import adapters.events.contents.NotificationType
import adapters.repositories.IRepository
import adapters.repositories.query_extend.QueryComparator
import adapters.repositories.query_extend.QueryBudgetExtend
import adapters.repositories.query_extend.QueryDateComparator
import domain.entities.Budget
import usecases.BackgroundTaskOut
import usecases.interfaces.ISuspendableUseCase
import domain.value_objects.Scheduler
import java.time.LocalDateTime

class UpdateDueBudget(
    private val budgetRepo: IRepository<Budget>,
    private val eventRegister: IEventRegister
): ISuspendableUseCase<Unit, BackgroundTaskOut> {
    override suspend fun execAsync(input: Unit): BackgroundTaskOut {
        try {
            val budgets = budgetRepo.getAll(
                query = QueryFilter(0, 0, true),
                QueryBudgetExtend(QueryDateComparator(
                    LocalDateTime.now(),
                    comparator = QueryComparator.LesserOrEquals
                )))

            for (budget in budgets.items.filter { !it.isArchived }) {
                if (budget.scheduler.repeater == null) {
                    budget.isArchived = true
                } else {
                    budget.scheduler = Scheduler(
                        budget.scheduler.upgradeDate()!!, // verifcation date already make in fuction
                        repeater = budget.scheduler.repeater,
                    )
                }

                budgetRepo.update(budget)

                if (budget.isArchived) {
                    eventRegister.notify(
                        EventType.NOTIFICATION, NotificationEventContent(
                        "Budget archived",
                        "Budget ${budget.title} a ete archive",
                            type = NotificationType.Success
                    ))
                } else {
                    eventRegister.notify(EventType.NOTIFICATION, NotificationEventContent(
                        "Mise a jour budget",
                        "Budget ${budget.title} a ete mis a jour",
                        NotificationType.Success
                    ))
                }

            }

            return BackgroundTaskOut("All due Budget updated")
        } catch (error: Throwable) {
            eventRegister.notify(EventType.NOTIFICATION, NotificationEventContent(
                "Error While update budget",
                "Error: ${error.message}",
                NotificationType.Error
            ))

            return BackgroundTaskOut("Error while update budget: ${error.localizedMessage}")
        }
    }
}