package dev.auguste.agni_api.core.usecases.profiles

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.DomainException
import dev.auguste.agni_api.core.entities.Profile
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.profiles.dto.UpdateProfileInput

class UpdateProfile(
    private val profileRepo: IRepository<Profile>
): IUseCase<UpdateProfileInput, Unit> {
    override fun execAsync(input: UpdateProfileInput) {
        val profile = profileRepo.get(input.id) ?: throw DomainException.NotFound.Profile(input.id)

        if (input.maxWishlistAmount != null) {
            profile.maxWishlistAmount = input.maxWishlistAmount
        }

        if (input.fixSpendPercentage != null) {
            profile.fixSpendPercentage = input.fixSpendPercentage
        }

        if (input.varialSpendPercentage != null) {
            profile.varialSpendPercentage = input.varialSpendPercentage
        }

        if (input.savingPercentage != null) {
            profile.savingPercentage = input.savingPercentage
        }

        if (profile.hasChanged())
            profileRepo.update(profile)
    }
}