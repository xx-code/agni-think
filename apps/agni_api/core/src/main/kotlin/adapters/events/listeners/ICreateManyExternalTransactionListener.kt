package adapters.events.listeners

import adapters.events.IEventListener
import adapters.events.contents.CreateManyEmbeddingExternalTransEventContent


interface ICreateManyExternalTransactionListener: IEventListener {
    fun server(content: CreateManyEmbeddingExternalTransEventContent)
}