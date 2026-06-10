package dev.auguste.agni_api.core.usecases.notifications

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.DomainException
import dev.auguste.agni_api.core.entities.Notification
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.notifications.dto.DeleteNotificationInput
import java.util.UUID

class DeleteNotification(private val notificationRepo: IRepository<Notification>): IUseCase<DeleteNotificationInput, Unit> {

    override fun execAsync(input: DeleteNotificationInput) {
        notificationRepo.get(input.notificationId) ?: throw DomainException.NotFound.Notification(input.notificationId)

        notificationRepo.delete(input.notificationId)
    }
}