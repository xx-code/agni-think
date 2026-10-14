package usecases.funds

import usecases.UseCase
import adapters.IAuthenticateAdapter
import adapters.repositories.IRepository
import domain.entities.Account
import domain.exceptions.AlreadyExistException
import domain.exceptions.NotFoundException
import domain.exceptions.ValidationException
import domain.entities.Fund
import domain.entities.User
import usecases.dto.CreatedOutput
import usecases.funds.dto.CreateFundInput
import domain.enums.FundType

class CreateFund(
    private val fundRepo: IRepository<Fund>,
    private val accountingRepo: IRepository<Account>,
    auth: IAuthenticateAdapter): UseCase<CreateFundInput, CreatedOutput>(auth = auth) {

    override suspend fun process(input: CreateFundInput): CreatedOutput {
        val currentUser = getCurrentUser()

        if (input.accountId != null && this.accountingRepo.get(input.accountId) == null)
            throw NotFoundException.SingleEntity(input.accountId, "account")

        if (fundRepo.existsByName(input.title))
            throw AlreadyExistException.EntitiesByField(mapOf("name" to input.title), "fund")

        if (input.type == FundType.AMORTIZATION && input.accountId != null)
            throw ValidationException.SavingGoalAmortizationFundMustNotHaveAccount()

        val newFund = Fund(
            userId = currentUser.id,
            title = input.title,
            description = input.description,
            accountId = input.accountId,
            target = input.target,
            type = input.type,
            balance = 0.0
        )

        fundRepo.create(newFund)

        return CreatedOutput(newFund.id)
    }
}