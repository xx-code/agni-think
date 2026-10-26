package usecases.users.dto

import java.util.UUID

data class GetUserOutput(
    val userId: UUID,
    val userName: String,
    val email: String,
    val apiToken: String
)
