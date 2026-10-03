package usecases.dto

import domain.exceptions.BaseException
import domain.exceptions.UnExpectedException

sealed class Result<out T>{
    data class Success<T>(val data: T): Result<T>()
    data class Failure(val error: BaseException): Result<Nothing>()

    val isSuccess: Boolean get() = this is Success
    val isFailure: Boolean get() = this is Failure

    fun getOrNull(): T? = (this as? Success)?.data
    fun exceptionOrNull(): Throwable? = (this as? Failure)?.error
    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Failure -> throw error
    }

    companion object {
        fun <T> success(data: T): Result<T> = Success(data)
        fun <T> fail(error: BaseException): Result<T> = Failure(error)
    }
}