package dev.auguste.agni_api.core.entities

import java.time.LocalDateTime
import java.util.UUID
import kotlin.properties.Delegates

class Notification(
    id: UUID = UUID.randomUUID(),
    title: String,
    content: String,
    dateTime: LocalDateTime = LocalDateTime.now(),
    isRead: Boolean = false,
): Entity(id = id) {
    val title = title
    val content = content
    val dateTime = dateTime
    var isRead by Delegates.observable(isRead) { _, old, new ->
        if (old != new)
            this.markHasChanged()
    }
}