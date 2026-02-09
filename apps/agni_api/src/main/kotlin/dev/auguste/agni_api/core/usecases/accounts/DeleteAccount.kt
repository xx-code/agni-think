package dev.auguste.agni_api.core.usecases.accounts

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Account
import dev.auguste.agni_api.core.usecases.accounts.dto.DeleteAccountInput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import java.util.UUID

class DeleteAccount(private val accountRepo: IRepository<Account>): IUseCase<DeleteAccountInput, Unit> {
    override fun execAsync(input: DeleteAccountInput) {
        accountRepo.get(input.accountId) ?: throw Error("Account ${input.accountId} not found")

        accountRepo.delete(input.accountId)
    }
}