package usecases.accounts

import adapters.IAuthenticateAdapter
import usecases.UseCase
import adapters.repositories.IRepository
import adapters.repositories.QueryComparator
import adapters.repositories.QueryExtendBuilder
import adapters.dto.QueryFilter
import domain.entities.Account
import usecases.dto.ListOutput
import usecases.accounts.dto.GetAccountOutput
import usecases.accounts.dto.GetAllAccountInput
class GetAllAccounts(
    private val accountRepo: IRepository<Account>,
    auth: IAuthenticateAdapter
): UseCase<GetAllAccountInput, ListOutput<GetAccountOutput>>(auth = auth) {

    override suspend fun process(input: GetAllAccountInput): ListOutput<GetAccountOutput> {
        val currentUser = getCurrentUser()
        val condition = QueryExtendBuilder<Account>()
            .addCondition("userId", QueryComparator.Equal, currentUser.id)
        val accounts = accountRepo.getAll(query = input.queryFilter, queryExtend = condition)

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