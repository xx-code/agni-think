package dev.auguste.agni_api.core.adapters.events.contents

import dev.auguste.agni_api.core.adapters.events.listeners.ICreateEmbeddingInvoiceEventListener
import dev.auguste.agni_api.core.adapters.events.IEventContent
import dev.auguste.agni_api.core.adapters.events.IEventListener
import dev.auguste.agni_api.core.entities.Invoice

class CreateEmbeddingInvoiceEventContent(
    val invoice: Invoice,
) : IEventContent {

    override fun dispatch(listener: IEventListener) {
        if (listener is ICreateEmbeddingInvoiceEventListener) {
            listener.serve(this)
            listener.update()
        }
    }
}