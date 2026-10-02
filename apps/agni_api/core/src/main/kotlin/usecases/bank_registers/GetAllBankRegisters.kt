package usecases.bank_registers

import adapters.dto.QueryFilter
import adapters.repositories.IRepository
import domain.entities.Account
import domain.entities.BankRegister
import usecases.ListOutput
import usecases.bank_registers.dto.AccountLinkerOutput
import usecases.bank_registers.dto.GetBankRegisterOutput
import usecases.interfaces.IUseCase

class GetAllBankRegisters(
    private val bankRegisterRepo: IRepository<BankRegister>,
    private val accountRepo: IRepository<Account>,
): IUseCase<QueryFilter, ListOutput<GetBankRegisterOutput>> {
    override fun execAsync(input: QueryFilter): ListOutput<GetBankRegisterOutput> {
        val res = bankRegisterRepo.getAll(input)
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