package usecases.notifications

import usecases.UseCase
import adapters.repositories.IRepository
import domain.exceptions.NotFoundException
import domain.entities.Notification
import usecases.notifications.dto.DeleteNotificationInput

class DeleteNotification(private val notificationRepo: IRepository<Notification>): UseCase<DeleteNotificationInput, Unit>() {

    override suspend fun process(input: DeleteNotificationInput) {
        notificationRepo.get(input.notificationId) ?: throw NotFoundException.SingleEntity(input.notificationId, "notification")

        notificationRepo.delete(input.notificationId)
    }
}