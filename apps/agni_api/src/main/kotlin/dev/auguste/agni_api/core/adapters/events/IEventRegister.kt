package dev.auguste.agni_api.core.adapters.events

interface IEventRegister {
    fun subscribe(event: String, listener: IEventListener)
    fun unsubscribe(event: String, listener: IEventListener)
    fun notify(event: String, content: EventContent)
}