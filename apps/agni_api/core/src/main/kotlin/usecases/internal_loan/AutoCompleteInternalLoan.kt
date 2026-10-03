package usecases.internal_loan

import adapters.dto.QueryFilter
import adapters.events.EventType
import adapters.events.IEventRegister
import adapters.events.contents.NotificationEventContent
import adapters.events.contents.NotificationType
import adapters.repositories.IRepository
import adapters.repositories.QueryExtendBuilder
import adapters.repositories.QueryComparator
import domain.entities.InternalLoan
import usecases.BackgroundTaskOut
import usecases.interfaces.ISuspendableUseCase
import usecases.interfaces.IUseCase
import usecases.invoices.dto.CompleteInvoiceInput
import usecases.invoices.dto.GetInvoiceOutput
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class AutoCompleteInternalLoan(
    private val internalLoanRepo: IRepository<InternalLoan>,
    private val getInvoice: IUseCase<UUID, GetInvoiceOutput>,
    private val completeInvoice: IUseCase<CompleteInvoiceInput, Unit>,
    private val eventRegister: IEventRegister
): ISuspendableUseCase<Unit, BackgroundTaskOut> {
    override suspend fun execAsync(input: Unit): BackgroundTaskOut {
        try {
            val condition = QueryExtendBuilder<InternalLoan>()
                .addCondition("dueDate", QueryComparator.LesserOrEquals, LocalDate.now())
            val internalLoans = internalLoanRepo.getAll(
                query = QueryFilter(0, 0, true),
                condition
            )

            for (internalLoan in internalLoans.items) {
                completeInvoice.execAsync(CompleteInvoiceInput(internalLoan.invoiceId))
                val invoice = getInvoice.execAsync(internalLoan.invoiceId)

                eventRegister.notify(
                    EventType.NOTIFICATION, NotificationEventContent(
                        "Pret interne",
                        "Pret pour ${invoice.transactions.first().description} est arrive a echeance",
                        type = NotificationType.Success
                    )
                )
            }

            return BackgroundTaskOut("All Internal load completed")
        } catch (error: Throwable) {
            eventRegister.notify(
                EventType.NOTIFICATION, NotificationEventContent(
                    "Error While update internal loan",
                    "Error: ${error.message}",
                    NotificationType.Error
                )
            )

            return BackgroundTaskOut("Error while update Internal loan: ${error.localizedMessage}")
        }
    }
}