package usecases.bank_registers

import usecases.UseCase
import adapters.IAuthenticateAdapter
import adapters.dto.QueryFilter
import adapters.repositories.IRepository
import adapters.repositories.QueryComparator
import adapters.repositories.QueryExtendBuilder
import domain.entities.Account
import domain.entities.BankRegister
import usecases.dto.ListOutput
import usecases.bank_registers.dto.AccountLinkerOutput
import usecases.bank_registers.dto.GetAllBankRegisterInput
import usecases.bank_registers.dto.GetBankRegisterOutput
class GetAllBankRegisters(
    private val bankRegisterRepo: IRepository<BankRegister>,
    private val accountRepo: IRepository<Account>,
    auth: IAuthenticateAdapter
): UseCase<GetAllBankRegisterInput, ListOutput<GetBankRegisterOutput>>(auth = auth) {
    override suspend fun process(input: GetAllBankRegisterInput): ListOutput<GetBankRegisterOutput> {
        val currentUser = getCurrentUser()
        val condition = QueryExtendBuilder<BankRegister>()
            .addCondition("userId", QueryComparator.Equal, currentUser.id)
        val res = bankRegisterRepo.getAll(input.queryFilter, condition)
        val accounts = accountRepo.getManyByIds(
            res.items.flatMap {
                it.accountsLinked.filter{ acc -> acc.accountId != null
            }.map { acc -> acc.accountId!! } }.toSet()
        )

        return ListOutput(
            res.items.map {
                GetBankRegisterOutput(
                    id = it.id,
                    institutionId = it.institutionId,
                    title = it.title,
                    accessCode = it.accessCode,
                    cursor = it.cursor,
                    isActive = it.isActive,
                    accounts = it.accountsLinked.map { accLink ->
                        val accountName = accounts.find { acc -> acc.id == accLink.accountId }?.title
                        AccountLinkerOutput(
                            accountId =  accLink.accountId,
                            bankRegisterId = accLink.bankAccountId,
                            accountName = accountName ?: "__NULL__",
                            bankAccountName = accLink.bankName
                        )
                    }
                )
            },
            res.total
        )
    }
}