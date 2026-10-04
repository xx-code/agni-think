package adapters.events

interface IEventListener {
    suspend fun update()
}