package usecases.profiles

import usecases.UseCase
import adapters.repositories.IRepository
import domain.exceptions.NotFoundException
import domain.entities.Profile
import usecases.profiles.dto.UpdateProfileInput

class UpdateProfile(
    private val profileRepo: IRepository<Profile>
): UseCase<UpdateProfileInput, Unit>() {
    override suspend fun process(input: UpdateProfileInput) {
        val profile = profileRepo.get(input.id) ?: throw NotFoundException.SingleEntity(input.id, "profile")

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

        if (input.balanceBuffer != null) {
            profile.balanceBuffer = input.balanceBuffer
        }

        if (profile.hasChanged())
            profileRepo.update(profile)
    }
}