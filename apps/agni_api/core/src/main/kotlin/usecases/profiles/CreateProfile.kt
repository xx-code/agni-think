package usecases.profiles

import adapters.repositories.IRepository
import domain.entities.Profile
import usecases.CreatedOutput
import usecases.interfaces.IUseCase
import usecases.profiles.dto.CreateProfileInput

class CreateProfile(
    private val profileRepo: IRepository<Profile>
): IUseCase<CreateProfileInput, CreatedOutput> {
    override fun execAsync(input: CreateProfileInput): CreatedOutput {
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