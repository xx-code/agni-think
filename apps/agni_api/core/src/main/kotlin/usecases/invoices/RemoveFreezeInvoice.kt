package usecases.invoices

import adapters.dto.QueryFilter
import adapters.events.EventType
import adapters.events.IEventRegister
import adapters.events.contents.NotificationEventContent
import adapters.events.contents.NotificationType
import adapters.repositories.IRepository
import adapters.repositories.QueryExtendBuilder
import adapters.repositories.QueryComparator
import domain.entities.Account
import domain.entities.Invoice
import domain.enums.InvoiceStatusType
import usecases.BackgroundTaskOut
import usecases.interfaces.IInnerUseCase
import usecases.interfaces.ISuspendableUseCase
import usecases.interfaces.IUseCase
import usecases.invoices.dto.DeleteInvoiceInput
import java.time.LocalDate
import java.time.LocalDateTime

class RemoveFreezeInvoice(
    private val invoiceRepo: IRepository<Invoice>,
    private val accountRepo: IRepository<Account>,
    private val deleteInvoice: IInnerUseCase<DeleteInvoiceInput, Unit>,
    private val eventRegister: IEventRegister
): ISuspendableUseCase<Unit, BackgroundTaskOut> {
    override suspend fun execAsync(input: Unit): BackgroundTaskOut {
        try {
            val condition = QueryExtendBuilder<Invoice>()
                .addCondition("is", QueryComparator.Equal, InvoiceStatusType.COMPLETED.value)
                .addCondition("date", QueryComparator.GreaterOrEquals, LocalDate.now())
            val freezeInvoice = invoiceRepo.getAll(
                query = QueryFilter(0, 0, true),
                queryExtend = condition
            )

            for(invoiceItem in freezeInvoice.items) {
                deleteInvoice.execAsync(DeleteInvoiceInput(invoiceItem.id, false))
                val account = accountRepo.get(invoiceItem.accountId)

                eventRegister.notify(EventType.NOTIFICATION, NotificationEventContent(
                    "Transaction degeler",
                    "Transaction ${account?.title} a une transaction geler",
                    NotificationType.Success
                ))
            }

            return BackgroundTaskOut("Remove Freeze transaction successfully")

        } catch (error: Throwable) {
            eventRegister.notify(EventType.NOTIFICATION, NotificationEventContent(
                "Error While degele invoice",
                "Error: ${error.message}",
                NotificationType.Error
            ))

            return BackgroundTaskOut(error.localizedMessage)
        }
    }
}