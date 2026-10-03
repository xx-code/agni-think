package usecases.notifications

import usecases.UseCase
import adapters.events.listeners.INotificationEventListener
import adapters.events.contents.NotificationEventContent
import adapters.repositories.IRepository
import domain.entities.Notification
import usecases.dto.CreatedOutput
import usecases.notifications.dto.PushNotificationInput

class PushNotification(private val notificationRepo: IRepository<Notification>): UseCase<PushNotificationInput, CreatedOutput>(), INotificationEventListener {
    private var event: NotificationEventContent? = null

    override suspend fun process(input: PushNotificationInput): CreatedOutput {
        val newNotification = Notification(
            title = input.title,
            content = input.content
        )

        notificationRepo.create(newNotification)

        return CreatedOutput(newNotification.id)
    }

    override suspend fun update() {
        event?.let {
            process(PushNotificationInput(
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