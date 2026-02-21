package dev.auguste.agni_api.core.adapters.events.listeners

import dev.auguste.agni_api.core.adapters.events.IEventListener
import dev.auguste.agni_api.core.adapters.events.contents.NotificationEventContent

interface INotificationEventListener : IEventListener {
    fun serve(content: NotificationEventContent)
}