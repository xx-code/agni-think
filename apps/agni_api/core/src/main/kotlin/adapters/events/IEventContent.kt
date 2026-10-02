package adapters.events

interface IEventContent {
    fun dispatch(listener: IEventListener)
}

