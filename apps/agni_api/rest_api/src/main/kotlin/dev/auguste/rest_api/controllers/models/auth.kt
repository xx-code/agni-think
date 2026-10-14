package dev.auguste.rest_api.controllers.models

import jakarta.validation.constraints.NotEmpty
import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDateTime
import java.util.UUID

data class ApiAuthLoginModel(
    val externalId: String,
    @field:NotEmpty("email not be empty")
    val email: String,
    @field:NotEmpty("username not be empty")
    val username: String
)

data class ApiAuthTokenModel(
    val token: String,
    val tokenType: String = "Bearer",
)