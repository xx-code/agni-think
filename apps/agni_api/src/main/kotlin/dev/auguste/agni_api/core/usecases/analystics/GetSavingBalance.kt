package dev.auguste.agni_api.core.usecases.analystics

import dev.auguste.agni_api.core.SAVING_CATEGORY_ID
import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Account
import dev.auguste.agni_api.core.entities.enums.AccountType
import dev.auguste.agni_api.core.usecases.analystics.dto.GetSavingBalanceInput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.invoices.dto.GetBalanceInput
import dev.auguste.agni_api.core.usecases.invoices.dto.GetBalanceOutput

class GetSavingBalance(
    private val accountRepo: IRepository<Account>,
    private val getBalance: IUseCase<GetBalanceInput, GetBalanceOutput>
) : IUseCase<GetSavingBalanceInput, Double> {
    override fun execAsync(input: GetSavingBalanceInput): Double {
         val accounts = accountRepo.getAll(QueryFilter(0, 0, true))
        val savingAccountType = setOf(AccountType.SAVING, AccountType.BROKING)
        val savingAccountIds = accounts.items.filter{ savingAccountType.contains(it.detail.getType()) }.map { it.id }

        val savingGoalBalance = getBalance.execAsync(GetBalanceInput(
            startDate = input.startDate,
            endDate = input.endDate,
            categoryIds = setOf(SAVING_CATEGORY_ID)
        ))

        val savingAccountBalance = getBalance.execAsync(GetBalanceInput(
            accountIds = savingAccountIds.toSet(),
            startDate = input.startDate,
            endDate = input.endDate,
            removeSystemCategory = false
        ))

        val total = savingGoalBalance.spend + savingAccountBalance.balance

        return if (total < 0) 0.0 else total
    }
}