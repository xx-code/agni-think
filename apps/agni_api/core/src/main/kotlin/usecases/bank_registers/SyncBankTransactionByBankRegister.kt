package usecases.bank_registers

import adapters.IAuthenticateAdapter
import adapters.IBankingHandlerAdapter
import adapters.dto.QueryFilter
import adapters.repositories.IRepository
import adapters.repositories.QueryComparator
import adapters.repositories.QueryExtendBuilder
import domain.entities.BankRegister
import domain.entities.ExternalTransaction
import domain.exceptions.ForbiddenException
import domain.exceptions.NotFoundException
import domain.exceptions.UnAuthorizeException
import usecases.UseCase
import usecases.bank_registers.dto.SyncBankTransactionInput
import java.util.UUID

class SyncBankTransactionByBankRegister(
    private val bankRegisterRepo: IRepository<BankRegister>,
    private val bankingHandlerAdapter: IBankingHandlerAdapter,
    auth: IAuthenticateAdapter
): UseCase<UUID, Unit>(auth=auth) {
    override suspend fun process(input: UUID) {
        val currentUser = getCurrentUser()
        val bankRegister = bankRegisterRepo.get(input) ?: throw NotFoundException.SingleEntity(input, "bank_register")
        if (bankRegister.userId != currentUser.id)
            throw ForbiddenException.UseCase()

        bankingHandlerAdapter.syncBankTransactions(listOf(bankRegister))
    }
}