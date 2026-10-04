package adapters.events.listeners

import adapters.events.IEventListener
import adapters.events.contents.DeleteEmbeddingInvoiceEventContent

interface IDeleteInvoiceEventListener : IEventListener {
    fun serve(content: DeleteEmbeddingInvoiceEventContent)
}