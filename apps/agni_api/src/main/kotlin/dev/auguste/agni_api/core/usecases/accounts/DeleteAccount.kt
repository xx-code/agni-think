package dev.auguste.agni_api.core.usecases.accounts

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Account
import dev.auguste.agni_api.core.entities.DomainException
import dev.auguste.agni_api.core.usecases.accounts.dto.DeleteAccountInput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase

class DeleteAccount(private val accountRepo: IRepository<Account>): IUseCase<DeleteAccountInput, Unit> {
    override fun execAsync(input: DeleteAccountInput) {
        accountRepo.get(input.accountId) ?: throw DomainException.NotFound.Account(input.accountId)

        accountRepo.delete(input.accountId)
    }
}