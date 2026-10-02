package usecases.accounts

import adapters.dto.QueryFilter
import adapters.repositories.IRepository
import adapters.repositories.query_extend.QueryInternalLoanExtend
import adapters.repositories.query_extend.QuerySavingGoalExtend
import domain.entities.Account
import domain.entities.InternalLoan
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
    private val internalLoanRepo: IRepository<InternalLoan>,
    private val getBalance: IUseCase<GetBalanceInput, GetBalanceOutput>,
    private val getInvoice: IUseCase<UUID, GetInvoiceOutput>
): IUseCase<UUID, GetAccountWithDetailOutput> {
    override fun execAsync(input: UUID): GetAccountWithDetailOutput {
        val account = accountRepo.get(input) ?: throw NotFoundException.SingleEntity(input, "account")
        val internalLoans = internalLoanRepo.getAll(QueryFilter(queryAll = true), QueryInternalLoanExtend(fundSourceId = input))
        var currentLoanBalance = 0.0
        if (internalLoans.items.isNotEmpty()) {
            internalLoans.items.forEach { internalLoanItem ->
                currentLoanBalance += getInvoice.execAsync(internalLoanItem.invoiceId).total
            }
        }

        val savingGoals = fundRepo.getAll(
            QueryFilter(0,0, true),
            QuerySavingGoalExtend(setOf(input)))

        val lockedBalance = savingGoals.items.sumOf { it.balance } + currentLoanBalance

        val freezeBalance = getBalance.execAsync(GetBalanceInput(
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