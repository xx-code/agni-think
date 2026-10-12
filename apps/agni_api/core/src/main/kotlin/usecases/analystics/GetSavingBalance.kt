package usecases.analystics

import adapters.IAuthenticateAdapter
import usecases.UseCase
import domain.SAVING_CATEGORY_ID
import adapters.dto.QueryFilter
import adapters.repositories.IRepository
import adapters.repositories.QueryComparator
import adapters.repositories.QueryExtendBuilder
import domain.entities.Account
import domain.entities.User
import domain.enums.AccountType
import usecases.analystics.dto.GetSavingBalanceInput
import usecases.interfaces.IUseCase
import usecases.invoices.dto.GetBalanceInput
import usecases.invoices.dto.GetBalanceOutput

class GetSavingBalance(
    private val accountRepo: IRepository<Account>,
    private val getBalance: IUseCase<GetBalanceInput, GetBalanceOutput>,
    auth: IAuthenticateAdapter
): UseCase<GetSavingBalanceInput, Double>(auth = auth) {
    override suspend fun process(input: GetSavingBalanceInput): Double {
        val currentUser = getCurrentUser()
        val condition = QueryExtendBuilder<Account>()
            .addCondition("userId", QueryComparator.Equal, currentUser.id)
        val accounts = accountRepo.getAll(QueryFilter.queryAll(), condition)
        val savingAccountType = setOf(AccountType.SAVING, AccountType.BROKING)
        val savingAccountIds = accounts.items.filter{ savingAccountType.contains(it.detail.getType()) }.map { it.id }

        val savingGoalBalance = getBalance.processDirect(GetBalanceInput(
            userId = currentUser.id,
            startDate = input.startDate,
            endDate = input.endDate,
            categoryIds = setOf(SAVING_CATEGORY_ID)
        ))

        val savingAccountBalance = getBalance.processDirect(GetBalanceInput(
            userId = currentUser.id,
            accountIds = savingAccountIds.toSet(),
            startDate = input.startDate,
            endDate = input.endDate,
            removeSystemCategory = false
        ))

        val total = savingGoalBalance.spend + savingAccountBalance.income

        return if (total < 0) 0.0 else total
    }
}