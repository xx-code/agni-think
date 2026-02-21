package dev.auguste.agni_api.core.adapters.events.contents

import dev.auguste.agni_api.core.adapters.events.IEventContent
import dev.auguste.agni_api.core.adapters.events.IEventListener
import dev.auguste.agni_api.core.adapters.events.listeners.INotificationEventListener

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