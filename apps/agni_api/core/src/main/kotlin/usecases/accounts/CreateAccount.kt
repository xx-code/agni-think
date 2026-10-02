package usecases.accounts

import adapters.repositories.IRepository
import domain.entities.Account
import domain.entities.Color
import domain.entities.Currency
import domain.exceptions.AlreadyExistException
import domain.exceptions.NotFoundException
import domain.exceptions.ValidationException
import usecases.CreatedOutput
import usecases.accounts.dto.CreateAccountInput
import usecases.interfaces.IUseCase

class CreateAccount(
    private val accountRepository: IRepository<Account>,
    private val currencyRepository: IRepository<Currency>
): IUseCase<CreateAccountInput, CreatedOutput> {

    override fun execAsync(input: CreateAccountInput): CreatedOutput {

        if (accountRepository.existsByName(input.title))
            throw AlreadyExistException.EntitiesByField(mapOf("name" to input.title), "account")

        if (input.currencyId != null)
            if (currencyRepository.get(input.currencyId) == null)
                throw NotFoundException.SingleEntity(input.currencyId, "currency")

        if (input.initBalance < 0)
            throw ValidationException.AccountBalanceMustBeGreaterThanZero()

        val newAccount = Account(
            title = input.title,
            currencyId = input.currencyId,
            balance = input.initBalance,
            detail = input.detail,
            color = Color(input.color)
        )

        accountRepository.create(newAccount)

        return CreatedOutput(newAccount.id)
    }
}