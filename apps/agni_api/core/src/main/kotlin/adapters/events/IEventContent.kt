package adapters.events

interface IEventContent {
    suspend fun dispatch(listener: IEventListener)
}

