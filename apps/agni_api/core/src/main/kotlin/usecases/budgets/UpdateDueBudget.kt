package usecases.budgets

import usecases.UseCase
import adapters.dto.QueryFilter
import adapters.events.EventType
import adapters.events.IEventRegister
import adapters.events.contents.NotificationEventContent
import adapters.events.contents.NotificationType
import adapters.repositories.IRepository
import adapters.repositories.QueryExtendBuilder
import adapters.repositories.QueryComparator
import domain.entities.Budget
import usecases.dto.BackgroundTaskOut
import domain.value_objects.Scheduler
import java.time.LocalDateTime

class UpdateDueBudget(
    private val budgetRepo: IRepository<Budget>,
    private val eventRegister: IEventRegister
): UseCase<Unit, BackgroundTaskOut>() {
    override suspend fun process(input: Unit): BackgroundTaskOut {
        try {
            val conditionBudget = QueryExtendBuilder<Budget>()
                .addCondition("scheduler.date", QueryComparator.GreaterOrEquals, LocalDateTime.now())

            val budgets = budgetRepo.getAll(
                query = QueryFilter(0, 0, true), conditionBudget)

            for (budget in budgets.items.filter { !it.isArchived }) {
                if (budget.scheduler.repeater == null) {
                    budget.isArchived = true
                } else {
                    budget.scheduler = Scheduler(
                        budget.scheduler.upgradeDate(),
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