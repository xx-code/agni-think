package dev.auguste.agni_api.core.usecases.notifications

import dev.auguste.agni_api.core.adapters.events.listeners.INotificationEventListener
import dev.auguste.agni_api.core.adapters.events.contents.NotificationEventContent
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Notification
import dev.auguste.agni_api.core.usecases.CreatedOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.notifications.dto.PushNotificationInput

class PushNotification(private val notificationRepo: IRepository<Notification>): IUseCase<PushNotificationInput, CreatedOutput>, INotificationEventListener {
    private var event: NotificationEventContent? = null

    override fun execAsync(input: PushNotificationInput): CreatedOutput {
        val newNotification = Notification(
            title = input.title,
            content = input.content
        )

        notificationRepo.create(newNotification)

        return CreatedOutput(newNotification.id)
    }

    override fun update() {
        event?.let {
            execAsync(PushNotificationInput(
                title = it.title,
                content = it.message
            ))
        }
        // free
        event = null
    }

    override fun serve(content: NotificationEventContent) {
        event = content
    }
}