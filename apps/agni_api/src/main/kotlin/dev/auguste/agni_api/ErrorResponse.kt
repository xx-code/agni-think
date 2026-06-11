package dev.auguste.agni_api

import java.time.Instant


data class ErrorResponse(
    val status: Int,
    val error: String,
    val message: String?,
    val timestamp: Instant = Instant.now()
)

data class ValidationErrorResponse(
    val status: Int = 400,
    val error: String = "Validation Error",
    val message: String = "La validation des données a échoué",
    val errors: Map<String, String?>,
    val timestamp: Instant = Instant.now()
)