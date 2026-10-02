package dev.auguste.agni_api.controllers

import dev.auguste.agni_api.controllers.models.ApiPushNotificationInput
import dev.auguste.agni_api.controllers.models.mapApiPushNotification
import adapters.dto.QueryFilter
import usecases.CreatedOutput
import usecases.ListOutput
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
import java.util.UUID

@RestController
@RequestMapping("/v2/notifications")
class NotificationController(
    private val pushNotificationUseCase: PushNotification,
    private val getAllNotificationsUseCase: IUseCase<QueryFilter, ListOutput<GetNotificationOutput>>,
    @Qualifier("togglePushNotification") private val toggleReadNotification: IUseCase<UUID, Unit>,
    private val deleteNotification: IUseCase<DeleteNotificationInput, Unit>
) {

    @PostMapping
    fun pushNotification(@Valid @RequestBody request: ApiPushNotificationInput) : ResponseEntity<CreatedOutput> {
        return ResponseEntity.ok(pushNotificationUseCase.execAsync(
            mapApiPushNotification(request)
        ))
    }

    @GetMapping
    fun getAllNotifications(query: QueryFilter) : ResponseEntity<ListOutput<GetNotificationOutput>> {
        return ResponseEntity.ok(getAllNotificationsUseCase.execAsync(
            query
        ))
    }

    @PutMapping("/{id}/toggle-read")
    fun toggleReadNotification(@PathVariable id: UUID) : ResponseEntity<Unit> {
        return ResponseEntity.ok(toggleReadNotification.execAsync(id))
    }

    @DeleteMapping("/{id}")
    fun deleteNotification(@PathVariable id: UUID) : ResponseEntity<Unit> {
        return ResponseEntity.ok(deleteNotification.execAsync(
            DeleteNotificationInput(id)
        ))
    }
}