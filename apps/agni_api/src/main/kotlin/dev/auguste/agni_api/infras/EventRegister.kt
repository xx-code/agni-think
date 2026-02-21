package dev.auguste.agni_api.infras

import dev.auguste.agni_api.core.adapters.events.EventType
import dev.auguste.agni_api.core.adapters.events.IEventContent
import dev.auguste.agni_api.core.adapters.events.IEventListener
import dev.auguste.agni_api.core.adapters.events.IEventRegister
import org.springframework.stereotype.Component

@Component
class EventRegister: IEventRegister {
    private val subscriptions: MutableMap<EventType, MutableSet<IEventListener>> = mutableMapOf()

    override fun subscribe(event: EventType, listener: IEventListener) {
        if (subscriptions.containsKey(event)) {
            subscriptions[event]?.add(listener)
        } else {
            subscriptions[event] = mutableSetOf(listener)
        }
    }

    override fun unsubscribe(event: EventType, listener: IEventListener) {
        if (subscriptions.containsKey(event)) {
            subscriptions[event]?.remove(listener)
        }
    }

    override fun notify(event: EventType, content: IEventContent) {
        if (subscriptions.containsKey(event)) {
            for (listener in subscriptions[event]!!) {
                content.dispatch(listener)
            }
        }
    }
}