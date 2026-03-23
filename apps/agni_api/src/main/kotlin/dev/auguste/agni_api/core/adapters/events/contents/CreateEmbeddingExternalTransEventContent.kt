package dev.auguste.agni_api.core.adapters.events.contents

import dev.auguste.agni_api.core.adapters.events.IEventContent
import dev.auguste.agni_api.core.adapters.events.IEventListener
import dev.auguste.agni_api.core.adapters.events.listeners.ICreateEmbeddingExternalTransListener
import dev.auguste.agni_api.core.entities.ExternalTransaction

class CreateEmbeddingExternalTransEventContent(
    val externalTransactions: ExternalTransaction
): IEventContent{
    override fun dispatch(listener: IEventListener) {
        if (listener is ICreateEmbeddingExternalTransListener) {
            listener.server(this)
            listener.update()
        }
    }
}