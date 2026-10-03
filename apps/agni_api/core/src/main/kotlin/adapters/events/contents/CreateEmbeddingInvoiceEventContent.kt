package adapters.events.contents

import adapters.events.listeners.ICreateInvoiceEventListener
import adapters.events.IEventContent
import adapters.events.IEventListener
import domain.entities.Invoice

class CreateEmbeddingInvoiceEventContent(
    val invoice: Invoice,
) : IEventContent {

    override fun dispatch(listener: IEventListener) {
        if (listener is ICreateInvoiceEventListener) {
            listener.serve(this)
            listener.update()
        }
    }
}