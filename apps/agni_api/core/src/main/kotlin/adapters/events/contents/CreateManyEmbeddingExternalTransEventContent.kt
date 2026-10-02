package adapters.events.contents

import adapters.events.IEventContent
import adapters.events.IEventListener
import adapters.events.listeners.ICreateManyExternalTransactionListener
import domain.entities.ExternalTransaction

class CreateManyEmbeddingExternalTransEventContent(
    val transactions: List<domain.entities.ExternalTransaction>
): IEventContent {
    override fun dispatch(listener: IEventListener) {
        if (listener is ICreateManyExternalTransactionListener) {
            listener.server(this)
            listener.update()
        }
    }
}