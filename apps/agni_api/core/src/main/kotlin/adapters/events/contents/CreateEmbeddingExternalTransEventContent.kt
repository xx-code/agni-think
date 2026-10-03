package adapters.events.contents

import adapters.events.IEventContent
import adapters.events.IEventListener
import adapters.events.listeners.ICreateExternalTransactionListener
import domain.entities.ExternalTransaction


class CreateEmbeddingExternalTransEventContent(
    val externalTransactions: ExternalTransaction
): IEventContent {
    override fun dispatch(listener: IEventListener) {
        if (listener is ICreateExternalTransactionListener) {
            listener.server(this)
            listener.update()
        }
    }
}