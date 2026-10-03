package usecase_configs

import adapters.repositories.IRepository
import domain.entities.Profile
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
        profileRepo: IRepository<Profile>
    ): UseCase<CreateProfileInput, CreatedOutput> {
        return CreateProfile(
            profileRepo = profileRepo
        )
    }

    @Bean
    fun updateProfile(
        profileRepo: IRepository<Profile>
    ): UseCase<UpdateProfileInput, Unit> {
        return UpdateProfile(
            profileRepo = profileRepo
        )
    }

    @Bean
    fun getProfile(
        profileRepo: IRepository<Profile>
    ): UseCase<UUID, GetProfileOutput> {
        return GetProfile(
            profileRepo = profileRepo
        )
    }
}