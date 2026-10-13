package dev.auguste.rest_api.controllers

import adapters.IAuthenticateAdapter
import adapters.dto.QueryFilter
import dev.auguste.rest_api.controllers.models.ApiPushNotificationInput
import dev.auguste.rest_api.controllers.models.mapApiPushNotification
import usecases.dto.CreatedOutput
import usecases.dto.ListOutput
import usecases.interfaces.IUseCase
import usecases.notifications.PushNotification
import usecases.notifications.dto.DeleteNotificationInput
import usecases.notifications.dto.GetNotificationOutput
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import usecases.notifications.dto.GetAllNotificationInput
import java.util.UUID

@RestController
@RequestMapping("/v2/notifications")
class NotificationController(
    private val pushNotificationUseCase: PushNotification,
    private val getAllNotificationsUseCase: IUseCase<GetAllNotificationInput, ListOutput<GetNotificationOutput>>,
    @Qualifier("togglePushNotification") private val toggleReadNotification: IUseCase<UUID, Unit>,
    private val deleteNotification: IUseCase<DeleteNotificationInput, Unit>,
    private val auth: IAuthenticateAdapter,
) {

    @PostMapping
    suspend fun pushNotification(@Valid @RequestBody request: ApiPushNotificationInput) : ResponseEntity<CreatedOutput> {
        val userId = auth.getCurrentUser().id
        return ResponseEntity.ok(pushNotificationUseCase.execute(
            mapApiPushNotification(userId, request)
        ).getOrThrow())
    }

    @GetMapping
    suspend fun getAllNotifications(query: QueryFilter) : ResponseEntity<ListOutput<GetNotificationOutput>> {
        return ResponseEntity.ok(getAllNotificationsUseCase.execute(
            GetAllNotificationInput(query)
        ).getOrThrow())
    }

    @PutMapping("/{id}/toggle-read")
    suspend fun toggleReadNotification(@PathVariable id: UUID) : ResponseEntity<Unit> {
        return ResponseEntity.ok(toggleReadNotification.execute(id).getOrThrow())
    }

    @DeleteMapping("/{id}")
    suspend fun deleteNotification(@PathVariable id: UUID) : ResponseEntity<Unit> {
        return ResponseEntity.ok(deleteNotification.execute(
            DeleteNotificationInput(id)
        ).getOrThrow())
    }
}