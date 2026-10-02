package adapters.events.contents

import adapters.events.IEventContent
import adapters.events.IEventListener
import adapters.events.listeners.INotificationEventListener

enum class NotificationType {
    Success,
    Error
}

class NotificationEventContent(
    val title: String,
    val message: String,
    val type: NotificationType
) : IEventContent {

    override fun dispatch(listener: IEventListener) {
        if (listener is INotificationEventListener) {
            listener.serve(this)
            listener.update()
        }
    }
}