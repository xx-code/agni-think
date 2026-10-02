package usecases.notifications

import adapters.events.listeners.INotificationEventListener
import adapters.events.contents.NotificationEventContent
import adapters.repositories.IRepository
import domain.entities.Notification
import usecases.CreatedOutput
import usecases.interfaces.IUseCase
import usecases.notifications.dto.PushNotificationInput

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