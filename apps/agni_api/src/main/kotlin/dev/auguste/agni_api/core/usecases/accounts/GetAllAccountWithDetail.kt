package dev.auguste.agni_api.core.usecases.accounts

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QuerySavingGoalExtend
import dev.auguste.agni_api.core.entities.Account
import dev.auguste.agni_api.core.entities.SavingGoal
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.accounts.dto.GetAccountWithDetailOutput
import dev.auguste.agni_api.core.usecases.accounts.dto.mapperAccountDetailOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.invoices.dto.GetBalanceInput
import dev.auguste.agni_api.core.usecases.invoices.dto.GetBalanceOutput

class GetAllAccountWithDetail(
    val accountRepo: IRepository<Account>,
    val savingGoalRepo: IRepository<SavingGoal>,
    val getBalance: IUseCase<GetBalanceInput, GetBalanceOutput>
) : IUseCase<QueryFilter, ListOutput<GetAccountWithDetailOutput>>{

    override fun execAsync(input: QueryFilter): ListOutput<GetAccountWithDetailOutput> {
        val accounts = accountRepo.getAll(input)
        val results = mutableListOf<GetAccountWithDetailOutput>()

        val savingGoals = savingGoalRepo.getAll(
            QueryFilter(0,0, true),
            QuerySavingGoalExtend(accounts.items.map { it.id }.toSet()))

        for(account in accounts.items) {
            val lockedBalance = savingGoals.items.filter { it.id == account.id }.sumOf { it.balance }
            val freezeBalance = getBalance.execAsync(GetBalanceInput(
                accountIds = setOf(account.id),
                isFreeze = true
            )).balance

            results.add(GetAccountWithDetailOutput(
                id = account.id,
                title = account.title,
                balance = account.balance,
                type = account.detail.getType(),
                lockedBalance = lockedBalance,
                freezeBalance = freezeBalance,
                detail = mapperAccountDetailOutput(account.detail, account.balance),
            ))
        }

        return ListOutput(
            items = results,
            total = accounts.total
        )
    }
}