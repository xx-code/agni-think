package dev.auguste.agni_api.controllers

import dev.auguste.agni_api.controllers.models.ApiPushNotificationInput
import dev.auguste.agni_api.controllers.models.mapApiPushNotification
import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.usecases.CreatedOutput
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.notifications.PushNotification
import dev.auguste.agni_api.core.usecases.notifications.dto.GetNotificationOutput
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v2/notifications")
class NotificationController(
    private val pushNotificationUseCase: PushNotification,
    private val getAllNotificationsUseCase: IUseCase<QueryFilter, ListOutput<GetNotificationOutput>>
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
}