package usecases.profiles

import adapters.repositories.IRepository
import domain.exceptions.NotFoundException
import domain.entities.Profile
import usecases.interfaces.IUseCase
import usecases.profiles.dto.GetProfileOutput
import java.util.UUID

class GetProfile(
    private val profileRepo: IRepository<Profile>
): IUseCase<UUID, GetProfileOutput> {
    override fun execAsync(input: UUID): GetProfileOutput {
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