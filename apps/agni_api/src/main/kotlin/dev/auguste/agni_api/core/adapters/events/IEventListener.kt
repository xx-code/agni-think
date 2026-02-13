package dev.auguste.agni_api.core.adapters.events

interface IEventListener {
    fun update(event: EventContent)
}