package usecases.notifications

import adapters.repositories.IRepository
import domain.entities.Notification
import usecases.interfaces.IUseCase
import java.util.UUID

class ToggleReadNotification(private val notificationRepo: IRepository<Notification>): IUseCase<UUID, Unit> {

    override fun execAsync(input: UUID) {
        val notification = notificationRepo.get(input)
        if (notification != null)  {
            notification.isRead = !notification.isRead
            notificationRepo.update(notification)
        }
    }
}