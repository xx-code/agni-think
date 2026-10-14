package usecases.notifications.dto

import java.util.UUID

data class PushNotificationInput(
    val userId: UUID,
    val title: String,
    val content: String
)
