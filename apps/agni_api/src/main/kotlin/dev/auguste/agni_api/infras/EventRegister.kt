package dev.auguste.agni_api.infras

import dev.auguste.agni_api.core.adapters.events.EventContent
import dev.auguste.agni_api.core.adapters.events.IEventListener
import dev.auguste.agni_api.core.adapters.events.IEventRegister
import org.springframework.stereotype.Component

@Component
class EventRegister: IEventRegister {
    private val subscriptions: MutableMap<String, MutableSet<IEventListener>> = mutableMapOf()

    override fun subscribe(event: String, listener: IEventListener) {
        if (subscriptions.containsKey(event)) {
            subscriptions[event]?.add(listener)
        } else {
            subscriptions[event] = mutableSetOf(listener)
        }
    }

    override fun unsubscribe(event: String, listener: IEventListener) {
        if (subscriptions.containsKey(event)) {
            subscriptions[event]?.remove(listener)
        }
    }

    override fun notify(event: String, content: EventContent) {
        if (subscriptions.containsKey(event)) {
            for (listener in subscriptions[event]!!) {
                listener.update(content)
            }
        }
    }
}