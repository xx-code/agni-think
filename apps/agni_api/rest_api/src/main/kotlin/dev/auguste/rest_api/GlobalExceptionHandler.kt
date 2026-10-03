package dev.auguste.rest_api

import com.fasterxml.jackson.databind.exc.MismatchedInputException
import dev.auguste.rest_api.i18n.MessageResolver
import domain.exceptions.AlreadyExistException
import domain.exceptions.BaseException
import domain.exceptions.NotFoundException
import domain.exceptions.UnExpectedException
import domain.exceptions.ValidationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler(
    private val messageResolver: MessageResolver,
) {
    @ExceptionHandler(NotFoundException::class)
    fun handleEntityNotFound(ex: NotFoundException): ResponseEntity<ErrorResponse> =
        respond(HttpStatus.NOT_FOUND, ex)

    @ExceptionHandler(AlreadyExistException::class)
    fun handleEntityAlreadyExist(ex: AlreadyExistException): ResponseEntity<ErrorResponse> =
        respond(HttpStatus.CONFLICT, ex)

    @ExceptionHandler(ValidationException::class)
    fun handleBusinessLogic(ex: ValidationException): ResponseEntity<ErrorResponse> =
        respond(HttpStatus.UNPROCESSABLE_ENTITY, ex)

    @ExceptionHandler(UnExpectedException::class)
    fun handleUnexpected(ex: UnExpectedException): ResponseEntity<ErrorResponse> =
        respond(HttpStatus.INTERNAL_SERVER_ERROR, ex)

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

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadable(ex: HttpMessageNotReadableException): ResponseEntity<ValidationErrorResponse> {
        val errorMap = mutableMapOf<String, String?>()

        val cause = ex.cause
        if (cause is MismatchedInputException) {
            val fieldName = cause.path.joinToString(".") { it.fieldName ?: "[${it.index}]" }
            errorMap[fieldName] = "Field '$fieldName' is missing or invalid"
        } else {
            errorMap["body"] = "Malformed JSON request body"
        }

        val validationErrorResponse = ValidationErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            errors = errorMap
        )

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validationErrorResponse)
    }

    private fun respond(status: HttpStatus, ex: BaseException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            status = status.value(),
            error = ex.errorKey,
            message = messageResolver.resolve(ex.errorKey, ex.metadata),
        )
        return ResponseEntity.status(status).body(errorResponse)
    }
}