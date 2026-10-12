package adapters

import domain.entities.User

interface IAuthenticateAdapter {
    suspend fun isAuthenticate(): Boolean
    suspend fun getCurrentUser(): User
}