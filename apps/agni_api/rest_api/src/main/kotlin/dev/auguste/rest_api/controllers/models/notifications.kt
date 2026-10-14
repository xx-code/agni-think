package dev.auguste.rest_api.controllers.models

import usecases.notifications.dto.PushNotificationInput
import jakarta.validation.constraints.NotEmpty
import java.util.UUID

data class ApiPushNotificationInput(
    @field:NotEmpty(message = "The title cannot be empty")
    val title: String,
    @field:NotEmpty(message = "The caption cannot be empty")
    val content: String
)

fun mapApiPushNotification(userId: UUID, model: ApiPushNotificationInput): PushNotificationInput {
    return PushNotificationInput(userId, model.title, model.content)
}