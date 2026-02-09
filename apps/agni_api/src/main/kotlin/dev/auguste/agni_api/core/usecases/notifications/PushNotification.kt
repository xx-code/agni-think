package dev.auguste.agni_api.core.usecases.notifications

import dev.auguste.agni_api.core.adapters.events.EventContent
import dev.auguste.agni_api.core.adapters.events.IEventListener
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Notification
import dev.auguste.agni_api.core.usecases.CreatedOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.notifications.dto.PushNotificationInput

class PushNotification(private val notificationRepo: IRepository<Notification>): IUseCase<PushNotificationInput, CreatedOutput>, IEventListener {

    override fun execAsync(input: PushNotificationInput): CreatedOutput {
        val newNotification = Notification(
            title = input.title,
            content = input.content
        )

        notificationRepo.create(newNotification)

        return CreatedOutput(newNotification.id)
    }

    override fun update(event: EventContent) {
        execAsync(PushNotificationInput(
            title = event.title,
            content = event.content
        ))
    }
}