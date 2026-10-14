package usecase_configs

import adapters.IAuthenticateAdapter
import adapters.repositories.IRepository
import domain.entities.Profile
import domain.entities.User
import usecases.dto.CreatedOutput
import usecases.interfaces.IUseCase
import usecases.profiles.CreateProfile
import usecases.profiles.GetProfile
import usecases.profiles.UpdateProfile
import usecases.profiles.dto.CreateProfileInput
import usecases.profiles.dto.GetProfileOutput
import usecases.profiles.dto.UpdateProfileInput
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import usecases.UseCase
import java.util.UUID

@Configuration
class ProfileConfig {

    @Bean
    fun createProfile(
        profileRepo: IRepository<Profile>,
        auth: IAuthenticateAdapter
    ): UseCase<CreateProfileInput, CreatedOutput> {
        return CreateProfile(
            auth = auth,
            profileRepo = profileRepo
        )
    }

    @Bean
    fun updateProfile(
        profileRepo: IRepository<Profile>,
        auth: IAuthenticateAdapter
    ): UseCase<UpdateProfileInput, Unit> {
        return UpdateProfile(
            profileRepo = profileRepo,
            auth = auth
        )
    }

    @Bean
    fun getProfile(
        profileRepo: IRepository<Profile>,
        auth: IAuthenticateAdapter
    ): UseCase<Unit, GetProfileOutput> {
        return GetProfile(
            auth = auth,
            profileRepo = profileRepo
        )
    }
}