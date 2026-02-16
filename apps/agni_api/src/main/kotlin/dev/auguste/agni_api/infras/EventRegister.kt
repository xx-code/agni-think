package dev.auguste.agni_api.infras

import dev.auguste.agni_api.core.adapters.events.IEventType
import dev.auguste.agni_api.core.adapters.events.IEventContent
import dev.auguste.agni_api.core.adapters.events.IEventListener
import dev.auguste.agni_api.core.adapters.events.IEventRegister
import org.springframework.stereotype.Component

@Component
class EventRegister: IEventRegister {
    private val subscriptions: MutableMap<IEventType, MutableSet<IEventListener>> = mutableMapOf()

    override fun subscribe(event: IEventType, listener: IEventListener) {
        if (subscriptions.containsKey(event)) {
            subscriptions[event]?.add(listener)
        } else {
            subscriptions[event] = mutableSetOf(listener)
        }
    }

    override fun unsubscribe(event: IEventType, listener: IEventListener) {
        if (subscriptions.containsKey(event)) {
            subscriptions[event]?.remove(listener)
        }
    }

    override fun notify(event: IEventType, content: IEventContent) {
        if (subscriptions.containsKey(event)) {
            for (listener in subscriptions[event]!!) {
                content.dispatch(listener)
            }
        }
    }
}