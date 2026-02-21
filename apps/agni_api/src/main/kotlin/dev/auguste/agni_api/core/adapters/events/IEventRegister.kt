package dev.auguste.agni_api.core.adapters.events

interface IEventRegister {
    fun subscribe(event: EventType, listener: IEventListener)
    fun unsubscribe(event: EventType, listener: IEventListener)
    fun notify(event: EventType, content: IEventContent)
}