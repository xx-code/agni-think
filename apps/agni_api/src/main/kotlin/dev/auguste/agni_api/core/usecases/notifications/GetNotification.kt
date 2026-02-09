package dev.auguste.agni_api.core.usecases.notifications

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Notification
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.notifications.dto.GetNotificationOutput
import java.util.UUID

class GetNotification(private val notificationRepo: IRepository<Notification>): IUseCase<UUID, GetNotificationOutput> {

    override fun execAsync(input: UUID): GetNotificationOutput {
        val notification = notificationRepo.get(input) ?: throw Error("Notification $input not found")

        return GetNotificationOutput(
            id = notification.id,
            title = notification.title,
            dateTime = notification.dateTime,
            isRead = notification.isRead,
            content = notification.content
        )
    }
}