package usecases.profiles

import adapters.IAuthenticateAdapter
import usecases.UseCase
import adapters.repositories.IRepository
import domain.entities.Profile
import domain.entities.User
import usecases.dto.CreatedOutput
import usecases.profiles.dto.CreateProfileInput

class CreateProfile(
    private val profileRepo: IRepository<Profile>,
    auth: IAuthenticateAdapter,
): UseCase<CreateProfileInput, CreatedOutput>(auth = auth) {
    override suspend fun process(input: CreateProfileInput): CreatedOutput {
        val currentUser = getCurrentUser()

        val newProfile = Profile(
            userId = currentUser.id,
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