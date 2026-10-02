package usecases.accounts

import adapters.repositories.IRepository
import adapters.dto.QueryFilter
import domain.entities.Account
import usecases.ListOutput
import usecases.accounts.dto.GetAccountOutput
import usecases.interfaces.IUseCase

class GetAllAccounts(private val accountRepo: IRepository<Account>): IUseCase<QueryFilter, ListOutput<GetAccountOutput>> {

    override fun execAsync(input: QueryFilter): ListOutput<GetAccountOutput> {
        val accounts = accountRepo.getAll(query = input)

        return ListOutput(
            items = accounts.items.map { GetAccountOutput(
                id = it.id,
                title = it.title,
                balance = it.balance,
                type = it.detail.getType().value,
                color = it.color.toString()
            )},
            total = accounts.total
        )
    }
}