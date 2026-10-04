package adapters.events.listeners

import adapters.events.IEventListener
import adapters.events.contents.NotificationEventContent

interface INotificationEventListener : IEventListener {
    fun serve(content: NotificationEventContent)
}