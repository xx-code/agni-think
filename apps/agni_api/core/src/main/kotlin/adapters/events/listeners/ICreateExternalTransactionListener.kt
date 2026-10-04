package adapters.events.listeners

import adapters.events.IEventListener
import adapters.events.contents.CreateEmbeddingExternalTransEventContent

interface ICreateExternalTransactionListener: IEventListener {
    fun server(content: CreateEmbeddingExternalTransEventContent)
}