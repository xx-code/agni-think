package usecases.notifications

import usecases.interfaces.IUseCase

import usecases.UseCase
import adapters.IAuthenticateAdapter
import usecases.notifications.dto.DeleteManyNotificationInput
import java.util.UUID

class DeleteManyNotification(
    auth: IAuthenticateAdapter
): UseCase<DeleteManyNotificationInput, Unit>(auth = auth) {
    override suspend fun process(input: DeleteManyNotificationInput) {
        TODO("Not yet implemented")
    }
}