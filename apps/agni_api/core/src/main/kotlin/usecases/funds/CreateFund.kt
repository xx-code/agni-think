package usecases.funds

import usecases.UseCase
import adapters.repositories.IRepository
import domain.entities.Account
import domain.exceptions.AlreadyExistException
import domain.exceptions.NotFoundException
import domain.exceptions.ValidationException
import domain.entities.Fund
import usecases.dto.CreatedOutput
import usecases.funds.dto.CreateFundInput
import domain.enums.FundType

class CreateFund(
    private val fundRepo: IRepository<Fund>,
    private val accountingRepo: IRepository<Account>): UseCase<CreateFundInput, CreatedOutput>() {

    override suspend fun process(input: CreateFundInput): CreatedOutput {
        if (input.accountId != null && this.accountingRepo.get(input.accountId) == null)
            throw NotFoundException.SingleEntity(input.accountId, "account")

        if (fundRepo.existsByName(input.title))
            throw AlreadyExistException.EntitiesByField(mapOf("name" to input.title), "saving_goal")

        if (input.type == FundType.AMORTIZATION && input.accountId != null)
            throw ValidationException.SavingGoalAmortizationFundMustNotHaveAccount()

        val newFund = Fund(
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