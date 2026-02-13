package dev.auguste.agni_api.core.usecases.notifications.dto

import java.util.UUID

data class DeleteNotificationInput(
    val notificationId: UUID
)
