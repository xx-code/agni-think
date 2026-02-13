package dev.auguste.agni_api.controllers.models

import dev.auguste.agni_api.core.usecases.notifications.dto.PushNotificationInput
import jakarta.validation.constraints.NotEmpty

data class ApiPushNotificationInput(
    @field:NotEmpty(message = "The title cannot be empty")
    val title: String,
    @field:NotEmpty(message = "The caption cannot be empty")
    val content: String
)

fun mapApiPushNotification(model: ApiPushNotificationInput): PushNotificationInput {
    return PushNotificationInput(model.title, model.content)
}