package usecases.goals

import adapters.IFinanceContext
import adapters.dto.QueryFilter
import adapters.events.EventType
import adapters.events.IEventRegister
import adapters.events.contents.NotificationEventContent
import adapters.events.contents.NotificationType
import adapters.repositories.IRepository
import adapters.repositories.QueryComparator
import adapters.repositories.QueryExtendBuilder
import domain.entities.Goal
import domain.enums.GoalStatusType
import domain.factories.GoalEvaluationStrategyFactory
import domain.value_objects.Scheduler
import usecases.UseCase
import usecases.dto.BackgroundTaskOut
import java.time.LocalDate

class SyncGoal(
    private val eventManager: IEventRegister,
    private val goalRepo: IRepository<Goal>,
    private val financeContext: IFinanceContext
): UseCase<Unit, BackgroundTaskOut>() {
    override suspend fun process(input: Unit): BackgroundTaskOut {
        try {
            val conditionGoal = QueryExtendBuilder<Goal>()
                .addCondition("status", QueryComparator.NotEqual, GoalStatusType.COMPLETED.ordinal)

            val res = goalRepo.getAll(
                QueryFilter.queryAll(),
                conditionGoal
            )

            for (goal in res.items) {
                val strategy = GoalEvaluationStrategyFactory.getStrategy(goal.type)
                val isGoalComplete = goal.isComplete(strategy, financeContext)

                if (isGoalComplete)
                    goal.status = GoalStatusType.COMPLETED

                if (goal.recurrence != null && goal.dueDate <= LocalDate.now()) {
                    val scheduler = Scheduler(goal.dueDate.atStartOfDay(), goal.recurrence)
                    goal.dueDate = scheduler.upgradeDate().toLocalDate()
                    goal.status = GoalStatusType.ACTIVE
                }

                if (goal.hasChanged()) {
                    goalRepo.update(goal)

                    val notificationTitle = "But"
                    val notificationMessage = if (goal.status == GoalStatusType.COMPLETED) {
                        "Le but ${goal.title} est complété"
                    } else {
                        "Le but ${goal.title} a repris, bonne chance !"
                    }

                    eventManager.notify(
                        EventType.NOTIFICATION,
                        NotificationEventContent(
                            title = notificationTitle,
                            message = notificationMessage,
                            type = NotificationType.Success,
                        )
                    )
                }
            }


            return BackgroundTaskOut("Sync Goal Success")
        } catch (error: Throwable) {
            this.eventManager.notify(
                EventType.NOTIFICATION, NotificationEventContent(
                    "Sync Goal  !Error",
                    "Error while Sync Goal: ${error.message}",
                    type = NotificationType.Error,
                )
            )

            return BackgroundTaskOut(error.localizedMessage)
        }
    }
}