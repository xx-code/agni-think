package dev.auguste.agni_api.core.usecases.accounts

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Account
import dev.auguste.agni_api.core.usecases.accounts.dto.UpdateAccountInput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase

class UpdateAccount(val accountRepo: IRepository<Account>): IUseCase<UpdateAccountInput, Unit> {

    override fun execAsync(input: UpdateAccountInput) {
        val account = accountRepo.get(input.id) ?: throw Error("Account ${input.id} not found")

        if (input.title != null) {
            if (input.title != account.title && accountRepo.existsByName(input.title))
                throw Error("Account ${input.title} already exists")

            account.title = input.title
        }

        if (input.detail != null) {
            account.detail = input.detail
        }

        if (account.hasChanged())
            accountRepo.update(account)
    }
}