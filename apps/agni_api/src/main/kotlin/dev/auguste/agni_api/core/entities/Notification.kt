package dev.auguste.agni_api.core.entities

import java.time.LocalDateTime
import java.util.Date
import java.util.UUID
import kotlin.properties.Delegates

class Notification(
    id: UUID = UUID.randomUUID(),
    val title: String,
    val content: String,
    val dateTime: LocalDateTime = LocalDateTime.now(),
    isRead: Boolean = false,
): Entity(id = id) {
    var isRead by Delegates.observable(isRead) { _, old, new ->
        if (old != new)
            this.markHasChanged()
    }
}