package usecases.profiles

import usecases.UseCase
import adapters.repositories.IRepository
import domain.entities.Profile
import usecases.dto.CreatedOutput
import usecases.profiles.dto.CreateProfileInput

class CreateProfile(
    private val profileRepo: IRepository<Profile>
): UseCase<CreateProfileInput, CreatedOutput>() {
    override suspend fun process(input: CreateProfileInput): CreatedOutput {
        val newProfile = Profile(
            fixSpendPercentage = input.fixSpendPercentage,
            maxWishlistAmount = input.maxWishlistAmount,
            savingPercentage = input.savingPercentage,
            varialSpendPercentage = input.varialSpendPercentage,
            balanceBuffer = input.balanceBuffer
        )

        profileRepo.create(newProfile)

        return CreatedOutput(newProfile.id)
    }
}