package usecases.bank_registers

import adapters.events.EventType
import adapters.events.IEventRegister
import adapters.events.contents.NotificationEventContent
import adapters.events.contents.NotificationType
import domain.entities.Notification
import usecases.UseCase
import usecases.bank_registers.dto.SyncBankTransactionInput
import usecases.dto.BackgroundTaskOut
import usecases.interfaces.IUseCase

class SyncBankTransactionTask(
    private val syncBankTransactions: IUseCase<SyncBankTransactionInput, Unit>,
    private val eventRegister: IEventRegister
): UseCase<Unit, BackgroundTaskOut>() {
    override suspend fun process(input: Unit): BackgroundTaskOut {
        try {
            syncBankTransactions.execute(SyncBankTransactionInput())
            return BackgroundTaskOut("All due Budget updated")
        } catch (error: Throwable) {
            eventRegister.notify(EventType.NOTIFICATION, NotificationEventContent(
                "Error While update budget",
                "Error: ${error.message}",
                NotificationType.Error,
                userId = Notification.SYSTEM_USER_ID
            ))

            return BackgroundTaskOut("Error while update budget: ${error.localizedMessage}")
        }
    }
}