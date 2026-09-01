package dev.auguste.agni_api.core.usecases.profiles

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Profile
import dev.auguste.agni_api.core.usecases.CreatedOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.profiles.dto.CreateProfileInput

class CreateProfile(
    private val profileRepo: IRepository<Profile>
): IUseCase<CreateProfileInput, CreatedOutput> {
    override fun execAsync(input: CreateProfileInput): CreatedOutput {
        val newProfile = Profile(
            fixSpendPercentage = input.fixSpendPercentage,
            maxWishlistAmount = input.maxWishlistAmount,
            savingPercentage = input.savingPercentage,
            varialSpendPercentage = input.varialSpendPercentage
        )

        profileRepo.create(newProfile)

        return CreatedOutput(newProfile.id)
    }
}