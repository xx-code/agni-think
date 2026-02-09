package dev.auguste.agni_api.core.adapters.repositories

interface IUnitOfWork {
    fun <T> execute(block: () -> T): T
}