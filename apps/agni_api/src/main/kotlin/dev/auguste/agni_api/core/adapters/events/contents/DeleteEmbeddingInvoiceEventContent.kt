package dev.auguste.agni_api.core.adapters.events.contents

import dev.auguste.agni_api.core.adapters.events.listeners.IDeleteEmbeddingInvoiceEventListener
import dev.auguste.agni_api.core.adapters.events.IEventContent
import dev.auguste.agni_api.core.adapters.events.IEventListener
import java.util.UUID

class DeleteEmbeddingInvoiceEventContent(
    val invoiceId: UUID,
) : IEventContent {

    override fun dispatch(listener: IEventListener) {
        if (listener is IDeleteEmbeddingInvoiceEventListener) {
            listener.serve(this)
            listener.update()
        }
    }
}