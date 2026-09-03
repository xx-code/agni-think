package dev.auguste.agni_api.core.usecases.spending_period_template

import dev.auguste.agni_api.core.adapters.events.EventType
import dev.auguste.agni_api.core.adapters.events.IEventRegister
import dev.auguste.agni_api.core.adapters.events.contents.NotificationEventContent
import dev.auguste.agni_api.core.adapters.events.contents.NotificationType
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.SpendingPeriod
import dev.auguste.agni_api.core.entities.SpendingPeriodTemplate
import dev.auguste.agni_api.core.usecases.BackgroundTaskOut
import dev.auguste.agni_api.core.usecases.interfaces.ISuspendableUseCase

class ApplySpendingPeriodTemplate(
    private val spendingPeriodTemplateRepo: IRepository<SpendingPeriodTemplate>,
    private val spendingPeriod: IRepository<SpendingPeriod>,
    private val eventManager: IEventRegister
): ISuspendableUseCase<Unit, BackgroundTaskOut> {
    override suspend fun execAsync(input: Unit): BackgroundTaskOut {
        try {
           TODO()
        } catch (error: Throwable) {
            this.eventManager.notify(
                EventType.NOTIFICATION, NotificationEventContent(
                    "Spending Period Factory Error",
                    "Error while applying schedule in voice ${error.message}",
                    type = NotificationType.Error,
                )
            )

            return BackgroundTaskOut(error.localizedMessage)
        }
    }
}