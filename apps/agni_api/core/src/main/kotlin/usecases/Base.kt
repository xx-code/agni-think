package usecases

import adapters.repositories.IRepository
import adapters.repositories.IUnitOfWork
import domain.exceptions.BaseException
import domain.exceptions.UnExpectedException
import usecases.dto.Result
import usecases.interfaces.IUseCase

abstract class UseCase<Tin, Tout>(
    protected val unitOfWork: IUnitOfWork? = null
): IUseCase<Tin, Tout> {
    /**
     * Entry point executing the use case within a unified `try-catch` wrapper.
     * Automatically dispatches to [IUnitOfWork] if provided.
     */
    final override suspend fun execute(input: Tin): Result<Tout> {
        return safeExecute {
            if (unitOfWork != null) {
                unitOfWork.execute { process(input) }
            } else {
                process(input)
            }
        }
    }

    /**
     * Internal domain logic implementation.
     *
     * Concrete use cases must override this method with their core business rules.
     * Exceptions thrown here will be caught and wrapped into a [Result.Failure].
     *
     * @param input The input payload.
     * @return The domain output payload [TOutput].
     * @throws BaseException Expected domain/business exception.
     * @throws Exception Unexpected technical runtime exception.
     */
    protected abstract suspend fun process(input: Tin): Tout

    /**
     * Entrée interne : réservée aux Use Cases qui composent dans la transaction de l'appelant.
     * Lève les exceptions et n'ouvre pas de nouvelle transaction.
     */
    override suspend fun processDirect(input: Tin): Tout {
        return process(input)
    }

    private inline fun safeExecute(block: () -> Tout): Result<Tout> {
        return try {
            Result.Success(block())
        } catch (e: BaseException) {
            Result.fail(e)
        } catch (e: Exception) {
            Result.fail(UnExpectedException(e.message ?: "An unexpected error occurred"))
        }
    }
}
