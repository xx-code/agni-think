package adapters.repositories

interface IUnitOfWork {
    suspend fun <T> execute(block: suspend () -> T): T
}