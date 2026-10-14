package usecase_configs

import adapters.IAuthenticateAdapter
import adapters.repositories.IRepository
import domain.entities.User
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import usecases.UseCase
import usecases.users.GetCurrentUser
import usecases.users.GetUser
import usecases.users.dto.GetUserOutput
import java.util.UUID

@Configuration
class UserConfig {

    @Bean
    fun getUser(userRepo: IRepository<User>): UseCase<UUID, GetUserOutput> {
        return GetUser(userRepo)
    }

    @Bean
    fun getCurrentUser(auth: IAuthenticateAdapter): UseCase<Unit, GetUserOutput> {
        return GetCurrentUser(auth)
    }
}
