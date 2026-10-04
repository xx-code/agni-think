package usecases.profiles

import usecases.UseCase
import adapters.repositories.IRepository
import domain.exceptions.NotFoundException
import domain.entities.Profile
import usecases.profiles.dto.GetProfileOutput
import java.util.UUID

class GetProfile(
    private val profileRepo: IRepository<Profile>
): UseCase<UUID, GetProfileOutput>() {
    override suspend fun process(input: UUID): GetProfileOutput {
        val profile = profileRepo.get(input) ?: throw NotFoundException.SingleEntity(input, "profile")
        return GetProfileOutput(
            maxWishlistAmount = profile.maxWishlistAmount,
            fixSpendPercentage = profile.fixSpendPercentage,
            varialSpendPercentage = profile.varialSpendPercentage,
            savingPercentage = profile.savingPercentage,
            balanceBuffer = profile.balanceBuffer
        )
    }
}