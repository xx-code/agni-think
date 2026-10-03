package usecases.analystics

import usecases.UseCase
import domain.SAVING_CATEGORY_ID
import adapters.dto.QueryFilter
import adapters.repositories.IRepository
import domain.entities.Account
import domain.enums.AccountType
import usecases.analystics.dto.GetSavingBalanceInput
import usecases.interfaces.IUseCase
import usecases.invoices.dto.GetBalanceInput
import usecases.invoices.dto.GetBalanceOutput

class GetSavingBalance(
    private val accountRepo: IRepository<Account>,
    private val getBalance: IUseCase<GetBalanceInput, GetBalanceOutput>
): UseCase<GetSavingBalanceInput, Double>() {
    override suspend fun process(input: GetSavingBalanceInput): Double {
         val accounts = accountRepo.getAll(QueryFilter(0, 0, true))
        val savingAccountType = setOf(AccountType.SAVING, AccountType.BROKING)
        val savingAccountIds = accounts.items.filter{ savingAccountType.contains(it.detail.getType()) }.map { it.id }

        val savingGoalBalance = getBalance.processDirect(GetBalanceInput(
            startDate = input.startDate,
            endDate = input.endDate,
            categoryIds = setOf(SAVING_CATEGORY_ID)
        ))

        val savingAccountBalance = getBalance.processDirect(GetBalanceInput(
            accountIds = savingAccountIds.toSet(),
            startDate = input.startDate,
            endDate = input.endDate,
            removeSystemCategory = false
        ))

        val total = savingGoalBalance.spend + savingAccountBalance.income

        return if (total < 0) 0.0 else total
    }
}