package usecases.notifications.dto

import java.util.UUID

data class DeleteManyNotificationInput(
    val ids: Set<UUID>,
    val isRead: Boolean? = null
)
