package usecases.funds

import usecases.UseCase
import adapters.repositories.IRepository
import domain.entities.Account
import domain.exceptions.AlreadyExistException
import domain.exceptions.NotFoundException
import domain.entities.Fund
import usecases.funds.dto.UpdateFundInput
import domain.enums.FundType

class UpdateFund(
    private val fundRepo: IRepository<Fund>,
    private val accountRepo: IRepository<Account>
): UseCase<UpdateFundInput, Unit>() {

    override suspend fun process(input: UpdateFundInput) {
        val savingGoal = fundRepo.get(input.id) ?: throw NotFoundException.SingleEntity(input.id, "saving_goal")

        if (input.title != null) {
            if (input.title != savingGoal.title && fundRepo.existsByName(input.title))
                throw AlreadyExistException.EntitiesByField(mapOf("name" to input.title), "saving_goal")

            savingGoal.title = input.title
        }

        if (input.target != null)
            savingGoal.target = input.target

        if (input.accountId != null) {
            if (accountRepo.get(input.accountId) == null)
                throw NotFoundException.SingleEntity(input.accountId, "account")

            savingGoal.accountId = input.accountId
        }

        if (input.description != null) {
            if (input.description != savingGoal.description) {
                savingGoal.description = input.description
            }
        }

        if (input.type != null && input.type != savingGoal.type ) {
            savingGoal.type = input.type
            if (input.type == FundType.AMORTIZATION)
                savingGoal.accountId = null
        }

        if (savingGoal.hasChanged())
            fundRepo.update(savingGoal)
    }
}