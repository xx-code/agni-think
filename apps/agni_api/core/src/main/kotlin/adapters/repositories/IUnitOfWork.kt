package adapters.repositories

interface IUnitOfWork {
    fun <T> execute(block: () -> T): T
}