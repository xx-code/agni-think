package adapters.events.listeners

import adapters.events.IEventListener
import adapters.events.contents.CreateEmbeddingInvoiceEventContent

interface ICreateInvoiceEventListener : IEventListener {
    fun serve(content: CreateEmbeddingInvoiceEventContent)
}