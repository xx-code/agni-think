package usecases.notifications

import usecases.interfaces.IUseCase

import usecases.UseCase
import usecases.notifications.dto.DeleteManyNotificationInput
import java.util.UUID

class DeleteManyNotification: UseCase<DeleteManyNotificationInput, Unit>() {
    override suspend fun process(input: DeleteManyNotificationInput) {
        TODO("Not yet implemented")
    }
}