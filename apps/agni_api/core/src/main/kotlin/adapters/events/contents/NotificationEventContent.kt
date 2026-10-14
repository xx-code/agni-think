package adapters.events.contents

import adapters.events.IEventContent
import adapters.events.IEventListener
import adapters.events.listeners.INotificationEventListener
import java.util.UUID

enum class NotificationType {
    Success,
    Error
}

class NotificationEventContent(
    val title: String,
    val message: String,
    val type: NotificationType,
    val userId: UUID
) : IEventContent {

    override suspend fun dispatch(listener: IEventListener) {
        if (listener is INotificationEventListener) {
            listener.serve(this)
            listener.update()
        }
    }
}