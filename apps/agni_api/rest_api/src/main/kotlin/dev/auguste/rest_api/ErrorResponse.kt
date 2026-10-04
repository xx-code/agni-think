package dev.auguste.rest_api

import java.time.Instant


data class ErrorResponse(
    val status: Int,
    val error: String,
    val message: String?,
    val timestamp: Instant = Instant.now()
)

data class ValidationErrorResponse(
    val status: Int = 400,
    val error: String = "VALIDATION_ERROR",
    val errors: Map<String, String?>,
    val timestamp: Instant = Instant.now()
)