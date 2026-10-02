package usecases.notifications

import adapters.repositories.IRepository
import domain.entities.Notification
import usecases.interfaces.IUseCase
import domain.exceptions.NotFoundException
import usecases.notifications.dto.GetNotificationOutput
import java.util.UUID

class GetNotification(private val notificationRepo: IRepository<Notification>): IUseCase<UUID, GetNotificationOutput> {

    override fun execAsync(input: UUID): GetNotificationOutput {
        val notification = notificationRepo.get(input) ?: throw NotFoundException.SingleEntity(input, "notification")

        return GetNotificationOutput(
            id = notification.id,
            title = notification.title,
            dateTime = notification.dateTime,
            isRead = notification.isRead,
            content = notification.content
        )
    }
}