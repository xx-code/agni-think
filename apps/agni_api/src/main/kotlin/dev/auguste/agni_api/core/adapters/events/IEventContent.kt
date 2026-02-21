package dev.auguste.agni_api.core.adapters.events

interface IEventContent {
    fun dispatch(listener: IEventListener)
}

