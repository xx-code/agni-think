package dev.auguste.agni_api.core.adapters.events.contents

import dev.auguste.agni_api.core.adapters.events.IEventContent
import dev.auguste.agni_api.core.adapters.events.IEventListener
import dev.auguste.agni_api.core.adapters.events.listeners.ICreateManyExternalTransactionListener
import dev.auguste.agni_api.core.entities.ExternalTransaction

class CreateManyEmbeddingExternalTransEventContent(
    val transactions: List<ExternalTransaction>
): IEventContent {
    override fun dispatch(listener: IEventListener) {
        if (listener is ICreateManyExternalTransactionListener) {
            listener.server(this)
            listener.update()
        }
    }
}