package dev.auguste.agni_api.infras.usecase_configs

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Profile
import dev.auguste.agni_api.core.usecases.CreatedOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.profiles.CreateProfile
import dev.auguste.agni_api.core.usecases.profiles.GetProfile
import dev.auguste.agni_api.core.usecases.profiles.UpdateProfile
import dev.auguste.agni_api.core.usecases.profiles.dto.CreateProfileInput
import dev.auguste.agni_api.core.usecases.profiles.dto.GetProfileOutput
import dev.auguste.agni_api.core.usecases.profiles.dto.UpdateProfileInput
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.UUID

@Configuration
class ProfileConfig {

    @Bean
    fun createProfile(
        profileRepo: IRepository<Profile>
    ): IUseCase<CreateProfileInput, CreatedOutput> {
        return CreateProfile(
            profileRepo = profileRepo
        )
    }

    @Bean
    fun updateProfile(
        profileRepo: IRepository<Profile>
    ): IUseCase<UpdateProfileInput, Unit> {
        return UpdateProfile(
            profileRepo = profileRepo
        )
    }

    @Bean
    fun getProfile(
        profileRepo: IRepository<Profile>
    ): IUseCase<UUID, GetProfileOutput> {
        return GetProfile(
            profileRepo = profileRepo
        )
    }
}