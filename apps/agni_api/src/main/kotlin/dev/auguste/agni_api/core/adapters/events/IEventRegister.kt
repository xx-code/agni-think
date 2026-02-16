package dev.auguste.agni_api.core.adapters.events

interface IEventRegister {
    fun subscribe(event: IEventType, listener: IEventListener)
    fun unsubscribe(event: IEventType, listener: IEventListener)
    fun notify(event: IEventType, content: IEventContent)
}