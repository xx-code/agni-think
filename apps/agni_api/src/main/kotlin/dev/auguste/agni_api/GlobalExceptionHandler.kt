package dev.auguste.agni_api

import dev.auguste.agni_api.core.entities.DomainException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(DomainException.NotFound::class)
    fun handleEntityNotFound(ex: DomainException.NotFound): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            status = HttpStatus.NOT_FOUND.value(),
            message = ex.message,
            error = ex.code,
        )
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse)
    }

    @ExceptionHandler(DomainException.AlreadyExist::class)
    fun handleEntityAlreadyExist(ex: DomainException.AlreadyExist): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            status = HttpStatus.CONFLICT.value(),
            message = ex.message,
            error = ex.code,
        )
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse)
    }

    @ExceptionHandler(DomainException.BusinessLogic::class)
    fun handleBusinessLogic(ex: DomainException.BusinessLogic): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            status = HttpStatus.UNPROCESSABLE_ENTITY.value(),
            message = ex.message,
            error = ex.code,
        )
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errorResponse)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(ex: MethodArgumentNotValidException): ResponseEntity<ValidationErrorResponse> {
        val fieldErrorsMap = ex.bindingResult.fieldErrors.associate { error ->
            error.field to error.defaultMessage
        }

        val validationErrorResponse = ValidationErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            errors = fieldErrorsMap
        )

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validationErrorResponse)
    }
}