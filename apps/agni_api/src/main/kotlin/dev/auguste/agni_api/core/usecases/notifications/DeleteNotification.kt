package dev.auguste.agni_api.core.usecases.notifications

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Notification
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import java.util.UUID

class DeleteNotification(val notificationRepo: IRepository<Notification>): IUseCase<UUID, Unit> {

    override fun execAsync(input: UUID) {
        notificationRepo.get(input) ?: throw Error("Notification $input not found")

        notificationRepo.delete(input)
    }
}