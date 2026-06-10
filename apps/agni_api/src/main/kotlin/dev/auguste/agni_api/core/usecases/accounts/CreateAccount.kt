package dev.auguste.agni_api.core.usecases.accounts

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Account
import dev.auguste.agni_api.core.entities.Currency
import dev.auguste.agni_api.core.entities.DomainException
import dev.auguste.agni_api.core.usecases.CreatedOutput
import dev.auguste.agni_api.core.usecases.accounts.dto.CreateAccountInput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase

class CreateAccount(
    private val accountRepository: IRepository<Account>,
    private val currencyRepository: IRepository<Currency>
): IUseCase<CreateAccountInput, CreatedOutput> {

    override fun execAsync(input: CreateAccountInput): CreatedOutput {

        if (accountRepository.existsByName(input.title))
            throw DomainException.AlreadyExist.Account(input.title)

        if (input.currencyId != null)
            if (currencyRepository.get(input.currencyId) == null)
                throw DomainException.NotFound.Currency(input.currencyId)

        if (input.initBalance < 0)
            throw DomainException.BusinessLogic.Validation("Balance must be greater than zero")

        val newAccount = Account(
            title = input.title,
            currencyId = input.currencyId,
            balance = input.initBalance,
            detail = input.detail)

        accountRepository.create(newAccount)

        return CreatedOutput(newAccount.id)
    }
}