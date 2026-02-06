package dev.auguste.agni_api.core.usecases.accounts

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Account
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import java.util.UUID

class DeleteAccount(val accountRepo: IRepository<Account>): IUseCase<UUID, Unit> {
    override fun execAsync(input: UUID) {
        accountRepo.get(input) ?: throw Error("Account $input not found")

        accountRepo.delete(input)
    }
}