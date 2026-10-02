package domain.entities

import java.time.LocalDateTime
import java.util.UUID


class Notification(
    id: UUID = UUID.randomUUID(),
    val title: String,
    val content: String,
    val dateTime: LocalDateTime = LocalDateTime.now(),
    isRead: Boolean = false,
): Entity(id = id) {
    var isRead by cleanObservable(isRead, this)
}