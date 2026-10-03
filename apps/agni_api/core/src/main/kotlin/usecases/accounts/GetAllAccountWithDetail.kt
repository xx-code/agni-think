package usecases.accounts

import adapters.dto.QueryFilter
import adapters.repositories.IRepository
import adapters.repositories.QueryExtendBuilder
import adapters.repositories.QueryComparator
import domain.entities.Account
import domain.entities.InternalLoan
import domain.entities.Fund
import usecases.ListOutput
import usecases.accounts.dto.GetAccountWithDetailOutput
import usecases.accounts.dto.mapperAccountDetailOutput
import usecases.interfaces.IUseCase
import usecases.invoices.dto.GetBalanceInput
import usecases.invoices.dto.GetBalanceOutput
import usecases.invoices.dto.GetInvoiceOutput
import java.util.UUID

class GetAllAccountWithDetail(
    private val accountRepo: IRepository<Account>,
    private val fundRepo: IRepository<Fund>,
    private val getBalance: IUseCase<GetBalanceInput, GetBalanceOutput>,
    private val internalLoanRepo: IRepository<InternalLoan>,
    private val getInvoice: IUseCase<UUID, GetInvoiceOutput>
) : IUseCase<QueryFilter, ListOutput<GetAccountWithDetailOutput>>{

    override fun execAsync(input: QueryFilter): ListOutput<GetAccountWithDetailOutput> {
        val accounts = accountRepo.getAll(input)
        val results = mutableListOf<GetAccountWithDetailOutput>()

        val conditionFund = QueryExtendBuilder<Fund>()
            .addCondition("accountId", QueryComparator.In, accounts.items.map { it.id }.toSet())
        val funds = fundRepo.getAll(
            QueryFilter(0,0, true), conditionFund)

        for(account in accounts.items) {
            val conditionInternalLoad = QueryExtendBuilder<InternalLoan>()
                .addCondition("fundSourceId", QueryComparator.Equal, account.id)
            val internalLoans = internalLoanRepo.getAll(QueryFilter(queryAll = true), conditionInternalLoad)
            var currentLoanBalance = 0.0
            if (internalLoans.items.isNotEmpty()) {
                internalLoans.items.forEach { internalLoanItem ->
                    currentLoanBalance += getInvoice.execAsync(internalLoanItem.invoiceId).total
                }
            }
            val lockedBalance = funds.items.filter { it.accountId == account.id }.sumOf { it.balance } + currentLoanBalance
            val freezeBalance = getBalance.execAsync(GetBalanceInput(
                accountIds = setOf(account.id),
                isFreeze = true
            )).balance

            results.add(GetAccountWithDetailOutput(
                id = account.id,
                title = account.title,
                balance = account.balance,
                type = account.detail.getType().value,
                lockedBalance = lockedBalance,
                freezeBalance = freezeBalance,
                detail = mapperAccountDetailOutput(account.detail, account.balance),
                color = account.color.toString()
            ))
        }

        return ListOutput(
            items = results,
            total = accounts.total
        )
    }
}