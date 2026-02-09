package dev.auguste.agni_api.core.usecases.accounts

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Account
import dev.auguste.agni_api.core.usecases.accounts.dto.GetAccountOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import java.util.UUID

class GetAccount(private val accountRepo: IRepository<Account>): IUseCase<UUID, GetAccountOutput>{

    override fun execAsync(input: UUID): GetAccountOutput {
        val account = accountRepo.get(input) ?: throw Error("Account with id $input not found")

        return GetAccountOutput(
            id = account.id,
            title = account.title,
            balance = account.balance,
            type = account.detail.getType()
        )
    }
}