package dev.auguste.agni_api.core.usecases.interfaces

import dev.auguste.agni_api.core.adapters.repositories.IUnitOfWork

interface IUseCase<TInput, TOut> {
    fun execAsync(input: TInput): TOut
}

interface ISuspendableUseCase<TInput, TOut> {
    suspend fun execAsync(input: TInput): TOut
}