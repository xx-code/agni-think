package dev.auguste.agni_api.core.usecases.accounts

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QuerySavingGoalExtend
import dev.auguste.agni_api.core.entities.Account
import dev.auguste.agni_api.core.entities.SavingGoal
import dev.auguste.agni_api.core.usecases.accounts.dto.GetAccountWithDetailOutput
import dev.auguste.agni_api.core.usecases.accounts.dto.mapperAccountDetailOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.invoices.dto.GetBalanceInput
import dev.auguste.agni_api.core.usecases.invoices.dto.GetBalanceOutput
import java.util.UUID

class GetAccountWithDetail(
    private val accountRepo: IRepository<Account>,
    private val savingGoalRepo: IRepository<SavingGoal>,
    private val getBalance: IUseCase<GetBalanceInput, GetBalanceOutput>
): IUseCase<UUID, GetAccountWithDetailOutput> {
    override fun execAsync(input: UUID): GetAccountWithDetailOutput {
        val account = accountRepo.get(input) ?: throw Error("Account not found")

        val savingGoals = savingGoalRepo.getAll(
            QueryFilter(0,0, true),
            QuerySavingGoalExtend(setOf(input)))

        val lockedBalance = savingGoals.items.sumOf { it.balance }

        val freezeBalance = getBalance.execAsync(GetBalanceInput(
            accountIds = setOf(account.id),
            isFreeze = true
        )).balance

        return GetAccountWithDetailOutput(
            id = account.id,
            title = account.title,
            balance = account.balance,
            type = account.detail.getType(),
            lockedBalance = lockedBalance,
            freezeBalance = freezeBalance,
            detail = mapperAccountDetailOutput(account.detail, account.balance),
        )
    }
}