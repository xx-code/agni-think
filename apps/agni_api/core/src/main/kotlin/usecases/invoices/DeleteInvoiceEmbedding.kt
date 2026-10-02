package usecases.invoices

import adapters.IEmbeddingService
import adapters.events.EventType
import adapters.events.listeners.IDeleteInvoiceEventListener
import adapters.events.IEventRegister
import adapters.events.contents.DeleteEmbeddingInvoiceEventContent
import adapters.events.contents.NotificationEventContent
import adapters.events.contents.NotificationType
import usecases.BackgroundTaskOut
import usecases.interfaces.IUseCase
import java.util.UUID

class DeleteInvoiceEmbedding(
    private val eventRegister: IEventRegister,
    private val embeddingService: IEmbeddingService,
    private val invoiceCollectionName: String
) : IUseCase<UUID, BackgroundTaskOut>, IDeleteInvoiceEventListener {
    private var event: DeleteEmbeddingInvoiceEventContent? = null

    override fun execAsync(input: UUID): BackgroundTaskOut {
        try {
            embeddingService.deleteEmbeddingDocument(invoiceCollectionName, input)

            return BackgroundTaskOut("Embedding invoice created")
        } catch (err: Exception) {
            eventRegister.notify(
                EventType.NOTIFICATION,
                NotificationEventContent(
                    title = "Fail to delete embedding invoice",
                    message = "Invoice was failed to create embedding invoice: ${err.message}",
                    type = NotificationType.Error
                )
            )

            return BackgroundTaskOut("Error while creating an invoice embedding. ${err.localizedMessage}")
        }
    }

    override fun update() {
        event?.let {
            execAsync(it.invoiceId)
        }

        event = null
    }

    override fun serve(content: DeleteEmbeddingInvoiceEventContent) {
        event = content
    }
}