package dev.auguste.agni_api.core.usecases.budgets

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.events.IEventType
import dev.auguste.agni_api.core.adapters.events.IEventRegister
import dev.auguste.agni_api.core.adapters.events.contents.NotificationEventContent
import dev.auguste.agni_api.core.adapters.events.contents.NotificationType
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.adapters.repositories.query_extend.ComparatorType
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryBudgetExtend
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryDateComparator
import dev.auguste.agni_api.core.entities.Budget
import dev.auguste.agni_api.core.usecases.BackgroundTaskOut
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.value_objects.Scheduler
import java.time.LocalDateTime

class UpdateDueBudget(
    private val budgetRepo: IRepository<Budget>,
    private val eventRegister: IEventRegister
): IUseCase<Unit, BackgroundTaskOut> {
    override fun execAsync(input: Unit): BackgroundTaskOut {
        try {
            val budgets = budgetRepo.getAll(
                query = QueryFilter(0, 0, true),
                QueryBudgetExtend(QueryDateComparator(
                    LocalDateTime.now(),
                    ComparatorType.LesserOrEquals
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
                        IEventType.NOTIFICATION, NotificationEventContent(
                        "Budget archived",
                        "Budget ${budget.title} a ete archive",
                            type = NotificationType.Success
                    ))
                } else {
                    eventRegister.notify(IEventType.NOTIFICATION, NotificationEventContent(
                        "Mise a jour budget",
                        "Budget ${budget.title} a ete mis a jour",
                        NotificationType.Success
                    ))
                }

            }

            return BackgroundTaskOut("All due Budget updated")
        } catch (error: Throwable) {
            eventRegister.notify(IEventType.NOTIFICATION, NotificationEventContent(
                "Error While update budget",
                "Error: ${error.message}",
                NotificationType.Error
            ))

            return BackgroundTaskOut("Error while update budget: ${error.localizedMessage}")
        }
    }
}