package usecases.users

import adapters.IAuthenticateAdapter
import usecases.UseCase
import usecases.users.dto.GetUserOutput

class GetCurrentUser(
    auth: IAuthenticateAdapter
): UseCase<Unit, GetUserOutput>(auth = auth) {
    override suspend fun process(input: Unit): GetUserOutput {
        val user = getCurrentUser()
        return GetUserOutput(
            userId = user.id,
            userName = user.username,
            email = user.email.toString()
        )
    }
}
