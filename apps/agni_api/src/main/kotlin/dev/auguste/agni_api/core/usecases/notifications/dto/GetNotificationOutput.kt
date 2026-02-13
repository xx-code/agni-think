package dev.auguste.agni_api.core.usecases.notifications.dto

import java.time.LocalDateTime
import java.util.Date
import java.util.UUID

data class GetNotificationOutput(
    val id: UUID,
    val title: String,
    val content: String,
    val isRead: Boolean,
    val dateTime: LocalDateTime
)
