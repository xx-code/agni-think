package usecases.accounts

import adapters.IAuthenticateAdapter
import usecases.UseCase
import adapters.dto.QueryFilter
import adapters.repositories.IRepository
import adapters.repositories.QueryExtendBuilder
import adapters.repositories.QueryComparator
import domain.entities.Account
import domain.entities.Fund
import usecases.accounts.dto.GetAccountWithDetailOutput
import usecases.accounts.dto.mapperAccountDetailOutput
import usecases.interfaces.IUseCase
import usecases.invoices.dto.GetBalanceInput
import usecases.invoices.dto.GetBalanceOutput
import usecases.invoices.dto.GetInvoiceOutput
import java.util.UUID
import domain.exceptions.NotFoundException

class GetAccountWithDetail(
    private val accountRepo: IRepository<Account>,
    private val fundRepo: IRepository<Fund>,
    private val getBalance: IUseCase<GetBalanceInput, GetBalanceOutput>,
    auth: IAuthenticateAdapter
): UseCase<UUID, GetAccountWithDetailOutput>(auth = auth) {
    override suspend fun process(input: UUID): GetAccountWithDetailOutput {
        val account = accountRepo.get(input) ?: throw NotFoundException.SingleEntity(input, "account")

        var currentLoanBalance = 0.0

        val conditionFund = QueryExtendBuilder<Fund>()
            .addCondition("accountId", QueryComparator.Equal, input)
        val funds = fundRepo.getAll(
            QueryFilter(0,0, true), conditionFund)

        val lockedBalance = funds.items.sumOf { it.balance } + currentLoanBalance

        val freezeBalance = getBalance.processDirect(GetBalanceInput(
            userId = account.userId,
            accountIds = setOf(account.id),
            isFreeze = true
        )).balance

        return GetAccountWithDetailOutput(
            id = account.id,
            title = account.title,
            balance = account.balance,
            type = account.detail.getType().value,
            lockedBalance = lockedBalance,
            freezeBalance = freezeBalance,
            detail = mapperAccountDetailOutput(account.detail, account.balance),
            color = account.color.toString()
        )
    }
}