package dev.auguste.mcp.controllers.models

data class ApiAuthLoginModel(
    val apiToken: String,
)

data class ApiAuthTokenModel(
    val token: String,
    val tokenType: String = "Bearer",
)
