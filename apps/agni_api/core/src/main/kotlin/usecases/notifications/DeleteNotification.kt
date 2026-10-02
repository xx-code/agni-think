package usecases.notifications

import adapters.repositories.IRepository
import domain.exceptions.NotFoundException
import domain.entities.Notification
import usecases.interfaces.IUseCase
import usecases.notifications.dto.DeleteNotificationInput

class DeleteNotification(private val notificationRepo: IRepository<Notification>): IUseCase<DeleteNotificationInput, Unit> {

    override fun execAsync(input: DeleteNotificationInput) {
        notificationRepo.get(input.notificationId) ?: throw NotFoundException.SingleEntity(input.notificationId, "notification")

        notificationRepo.delete(input.notificationId)
    }
}