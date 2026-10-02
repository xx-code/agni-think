package adapters.events.contents

import adapters.events.listeners.IDeleteInvoiceEventListener
import adapters.events.IEventContent
import adapters.events.IEventListener
import java.util.UUID

class DeleteEmbeddingInvoiceEventContent(
    val invoiceId: UUID,
) : IEventContent {

    override fun dispatch(listener: IEventListener) {
        if (listener is IDeleteInvoiceEventListener) {
            listener.serve(this)
            listener.update()
        }
    }
}