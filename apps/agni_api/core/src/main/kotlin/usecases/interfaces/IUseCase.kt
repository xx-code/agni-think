package usecases.interfaces

import adapters.repositories.IUnitOfWork
import usecases.DeleteOutput

interface IUseCase<TInput, TOut> {
    fun execAsync(input: TInput): TOut
}

interface ISuspendableUseCase<TInput, TOut> {
    suspend fun execAsync(input: TInput): TOut
}