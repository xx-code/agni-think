package dev.auguste.agni_api.core.adapters.events.listeners

import dev.auguste.agni_api.core.adapters.events.IEventListener
import dev.auguste.agni_api.core.adapters.events.contents.CreateEmbeddingInvoiceEventContent

interface ICreateEmbeddingInvoiceEventListener : IEventListener {
    fun serve(content: CreateEmbeddingInvoiceEventContent)
}