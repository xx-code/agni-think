package usecases.notifications

import usecases.UseCase
import adapters.repositories.IRepository
import domain.entities.Notification
import java.util.UUID

class ToggleReadNotification(private val notificationRepo: IRepository<Notification>): UseCase<UUID, Unit>() {

    override suspend fun process(input: UUID) {
        val notification = notificationRepo.get(input)
        if (notification != null)  {
            notification.isRead = !notification.isRead
            notificationRepo.update(notification)
        }
    }
}