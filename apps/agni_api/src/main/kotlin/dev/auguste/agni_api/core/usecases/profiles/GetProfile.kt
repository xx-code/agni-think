package dev.auguste.agni_api.core.usecases.profiles

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.DomainException
import dev.auguste.agni_api.core.entities.Profile
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.profiles.dto.GetProfileOutput
import java.util.UUID

class GetProfile(
    private val profileRepo: IRepository<Profile>
): IUseCase<UUID, GetProfileOutput> {
    override fun execAsync(input: UUID): GetProfileOutput {
        val profile = profileRepo.get(input) ?: throw DomainException.NotFound.Profile(input)
        return GetProfileOutput(
            maxWishlistAmount = profile.maxWishlistAmount,
            fixSpendPercentage = profile.fixSpendPercentage,
            varialSpendPercentage = profile.varialSpendPercentage,
            savingPercentage = profile.savingPercentage
        )
    }
}