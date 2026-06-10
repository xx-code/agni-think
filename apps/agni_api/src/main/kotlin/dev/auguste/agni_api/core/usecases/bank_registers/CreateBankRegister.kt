package dev.auguste.agni_api.core.usecases.bank_registers

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Account
import dev.auguste.agni_api.core.entities.BankRegister
import dev.auguste.agni_api.core.usecases.CreatedOutput
import dev.auguste.agni_api.core.usecases.bank_registers.dto.CreateBankRegisterInput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.value_objects.AccountLinked

class CreateBankRegister(
    private val bankRegisterRepo: IRepository<BankRegister>,
    private val accountRepo: IRepository<Account>,
): IUseCase<CreateBankRegisterInput, CreatedOutput> {
    override fun execAsync(input: CreateBankRegisterInput): CreatedOutput {
        val accounts = accountRepo.getManyByIds(input.accounts.map { it.accountId }.toSet())
        if (accounts.size != input.accounts.size)
                throw dev.auguste.agni_api.core.entities.DomainException.BusinessLogic.SomeAccountsNotFound()
        val newBankRegister = BankRegister(
            title = input.title,
            accessCode = input.accessCode,
            accountsLinked = input.accounts.map { AccountLinked(it.accountId, it.bankAccountId) }.toSet(),
        )

        bankRegisterRepo.create(newBankRegister)

        return CreatedOutput(newBankRegister.id)
    }
}