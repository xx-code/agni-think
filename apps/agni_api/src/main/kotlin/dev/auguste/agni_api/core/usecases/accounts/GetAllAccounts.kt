package dev.auguste.agni_api.core.usecases.accounts

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.entities.Account
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.accounts.dto.GetAccountOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase

class GetAllAccounts(private val accountRepo: IRepository<Account>): IUseCase<QueryFilter, ListOutput<GetAccountOutput>> {

    override fun execAsync(input: QueryFilter): ListOutput<GetAccountOutput> {
        val accounts = accountRepo.getAll(query = input)

        return ListOutput(
            items = accounts.items.map { GetAccountOutput(
                id = it.id,
                title = it.title,
                balance = it.balance,
                type = it.detail.getType()
            )},
            total = accounts.total
        )
    }
}