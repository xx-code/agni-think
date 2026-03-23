package dev.auguste.agni_api.core.usecases.bank_registers

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Account
import dev.auguste.agni_api.core.entities.BankRegister
import dev.auguste.agni_api.core.usecases.bank_registers.dto.UpdateBankRegisterInput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.value_objects.AccountLinked

class UpdateBankRegister(
    private val bankRegisterRepo: IRepository<BankRegister>,
    private val accountRepo: IRepository<Account>,
): IUseCase<UpdateBankRegisterInput, Unit> {
    override fun execAsync(input: UpdateBankRegisterInput) {
        val bankRegister = bankRegisterRepo.get(input.bankRegisterId) ?: throw Error("Bank Register ID ${input.bankRegisterId} not found")

        if (!input.accessCode.isNullOrEmpty()) {
           bankRegister.accessCode = input.accessCode
        }

        if (!input.accounts.isNullOrEmpty()) {
            val accounts = accountRepo.getManyByIds(input.accounts.map { it.accountId }.toSet())
            if (accounts.size != input.accounts.size)
                throw Error("SOME_ACCOUNTS_NOT_FOUND")

            bankRegister.accountslinked = input.accounts.map { AccountLinked(it.accountId, it.bankAccountId) }.toSet()
        }

        if (!input.title.isNullOrEmpty()) {
            bankRegister.title = input.title
        }

        if (!input.cursor.isNullOrEmpty())
            bankRegister.cursor = input.cursor

        if (bankRegister.hasChanged())
            bankRegisterRepo.update(bankRegister)
    }
}