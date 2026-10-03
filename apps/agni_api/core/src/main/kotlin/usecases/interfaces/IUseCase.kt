package usecases.interfaces

import usecases.interfaces.IUseCase

import usecases.dto.Result

/**
 * Generic contract for all business use cases in the application.
 *
 * Enforces a standardized execution signature taking a single input and returning
 * an explicit [Result] containing either the expected [TOutput] payload or an exception.
 *
 * @param TInput The type of input payload/command required to execute the use case.
 * @param TOutput The type of output data returned on successful execution.
 */
interface IUseCase<in TInput, out TOutput> {
    /**
     * Executes the use case asynchronously.
     *
     * @param input The input parameters required for execution.
     * @return A [Result] encapsulating either [Result.Success] or [Result.Failure].
     */
    suspend fun execute(input: TInput): Result<TOutput>
    /**
     * Point d'entree interne pour la composition entre use cases.
     *
     * Ne capture pas les exceptions et n'ouvre pas de transaction : l'appelant
     * (qu'il fournisse ou non une [adapters.repositories.IUnitOfWork]) fournit
     * le contexte et fait remonter les erreurs. A utiliser uniquement pour une
     * cascade interne, jamais depuis l'exterieur.
     *
     * @param input The input parameters required for execution.
     * @return The raw [TOutput] payload, or a thrown exception on failure.
     */
    suspend fun processDirect(input: TInput): TOutput
    /**
     * Allows invoking the use case directly as a function instance (e.g., `myUseCase(input)`).
     */
    suspend operator fun invoke(input: TInput): Result<TOutput> = execute(input)
}