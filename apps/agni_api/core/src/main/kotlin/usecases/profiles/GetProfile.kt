package usecases.profiles

import adapters.IAuthenticateAdapter
import adapters.dto.QueryFilter
import usecases.UseCase
import adapters.repositories.IRepository
import adapters.repositories.QueryComparator
import adapters.repositories.QueryExtendBuilder
import domain.exceptions.NotFoundException
import domain.entities.Profile
import usecases.profiles.dto.GetProfileOutput

class GetProfile(
    private val profileRepo: IRepository<Profile>,
    auth: IAuthenticateAdapter
): UseCase<Unit, GetProfileOutput>(auth = auth) {
    override suspend fun process(input: Unit): GetProfileOutput {
        val currentUser = getCurrentUser()
        val condition = QueryExtendBuilder<Profile>()
            .addCondition("userId", QueryComparator.Equal, currentUser.id)
        val profile = profileRepo.getAll(QueryFilter.queryAll(), condition).items.firstOrNull()
            ?: throw NotFoundException.SingleEntity(currentUser.id, "profile")
        return GetProfileOutput(
            maxWishlistAmount = profile.maxWishlistAmount,
            fixSpendPercentage = profile.fixSpendPercentage,
            varialSpendPercentage = profile.varialSpendPercentage,
            savingPercentage = profile.savingPercentage,
            balanceBuffer = profile.balanceBuffer
        )
    }
}