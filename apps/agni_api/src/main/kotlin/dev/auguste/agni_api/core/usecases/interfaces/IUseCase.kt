package dev.auguste.agni_api.core.usecases.interfaces

import dev.auguste.agni_api.core.adapters.repositories.IUnitOfWork

interface IUseCase<TInput, TOuput> {
    fun execAsync(input: TInput): TOuput
}