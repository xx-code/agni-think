package dev.auguste.agni_api.core.usecases.internal_loan

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.events.EventType
import dev.auguste.agni_api.core.adapters.events.IEventRegister
import dev.auguste.agni_api.core.adapters.events.contents.NotificationEventContent
import dev.auguste.agni_api.core.adapters.events.contents.NotificationType
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.adapters.repositories.query_extend.ComparatorType
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryDateComparator
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryInternalLoanExtend
import dev.auguste.agni_api.core.entities.InternalLoan
import dev.auguste.agni_api.core.usecases.BackgroundTaskOut
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.invoices.dto.CompleteInvoiceInput
import dev.auguste.agni_api.core.usecases.invoices.dto.GetInvoiceOutput
import java.time.LocalDateTime
import java.util.UUID

class AutoCompleteInternalLoan(
    private val internalLoanRepo: IRepository<InternalLoan>,
    private val getInvoice: IUseCase<UUID, GetInvoiceOutput>,
    private val completeInvoice: IUseCase<CompleteInvoiceInput, Unit>,
    private val eventRegister: IEventRegister
): IUseCase<Unit, BackgroundTaskOut> {
    override fun execAsync(input: Unit): BackgroundTaskOut {
        try {
            val internalLoans = internalLoanRepo.getAll(
                query = QueryFilter(0, 0, true),
                QueryInternalLoanExtend(
                    scheduleDueDateComparator = QueryDateComparator(
                        date = LocalDateTime.now(),
                        comparator = ComparatorType.GreaterOrEquals
                    )
                )
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