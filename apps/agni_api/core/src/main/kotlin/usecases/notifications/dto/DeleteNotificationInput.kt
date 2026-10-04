package usecases.notifications.dto

import java.util.UUID

data class DeleteNotificationInput(
    val notificationId: UUID
)
