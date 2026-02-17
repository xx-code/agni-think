package dev.auguste.agni_api.core.usecases.invoices

import dev.auguste.agni_api.core.adapters.IEmbeddingService
import dev.auguste.agni_api.core.adapters.events.EventType
import dev.auguste.agni_api.core.adapters.events.listeners.IDeleteEmbeddingInvoiceEventListener
import dev.auguste.agni_api.core.adapters.events.IEventRegister
import dev.auguste.agni_api.core.adapters.events.contents.DeleteEmbeddingInvoiceEventContent
import dev.auguste.agni_api.core.adapters.events.contents.NotificationEventContent
import dev.auguste.agni_api.core.adapters.events.contents.NotificationType
import dev.auguste.agni_api.core.usecases.BackgroundTaskOut
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import java.util.UUID

class DeleteInvoiceEmbedding(
    private val eventRegister: IEventRegister,
    private val embeddingService: IEmbeddingService
) : IUseCase<UUID, BackgroundTaskOut>, IDeleteEmbeddingInvoiceEventListener {
    private var event: DeleteEmbeddingInvoiceEventContent? = null

    override fun execAsync(input: UUID): BackgroundTaskOut {
        try {
            embeddingService.deleteEmbeddingDocument(input)

            eventRegister.notify(
                EventType.NOTIFICATION,
                NotificationEventContent(
                    title = "Transaction Embedding Deleted",
                    message = "Invoice was created: $input",
                    type = NotificationType.Success
                )
            )

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