package usecases.bank_registers

import adapters.IBankingHandlerAdapter
import adapters.dto.QueryFilter
import adapters.repositories.IRepository
import adapters.repositories.IUnitOfWork
import adapters.repositories.QueryComparator
import adapters.repositories.QueryExtendBuilder
import domain.entities.BankRegister
import domain.entities.ExternalTransaction
import domain.exceptions.ForbiddenException
import usecases.UseCase
import usecases.bank_registers.dto.SyncBankTransactionInput

class SyncBankTransaction(
    private val bankRegisterRepo: IRepository<BankRegister>,
    private val bankingHandlerAdapter: IBankingHandlerAdapter,
): UseCase<SyncBankTransactionInput, Unit>() {
    override suspend fun process(input: SyncBankTransactionInput) {
        var condition: QueryExtendBuilder<BankRegister>? = null
        if (input.userId != null) {
            condition = QueryExtendBuilder()
            condition.addCondition("userId", QueryComparator.Equal, input.userId)
        }

        val bankRegisters = bankRegisterRepo.getAll(QueryFilter.queryAll(), condition)
        if (input.userId != null && bankRegisters.items.map { it.userId }.any { it != input.userId })
            throw ForbiddenException.UseCase()

        bankingHandlerAdapter.syncBankTransactions(bankRegisters.items)
    }
}