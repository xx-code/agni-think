package dev.auguste.agni_api.core.adapters.repositories

interface IUnitOfWork {
    fun start()
    fun commit()
    fun rollback()
}