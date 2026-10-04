package usecases.accounts

import usecases.UseCase
import adapters.repositories.IRepository
import adapters.dto.QueryFilter
import domain.entities.Account
import usecases.dto.ListOutput
import usecases.accounts.dto.GetAccountOutput
class GetAllAccounts(private val accountRepo: IRepository<Account>): UseCase<QueryFilter, ListOutput<GetAccountOutput>>() {

    override suspend fun process(input: QueryFilter): ListOutput<GetAccountOutput> {
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