package dev.auguste.agni_api.core.usecases.bank_registers

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Account
import dev.auguste.agni_api.core.entities.BankRegister
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.bank_registers.dto.AccountLinkerOutput
import dev.auguste.agni_api.core.usecases.bank_registers.dto.GetBankRegisterOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase

class GetAllBankRegisters(
    private val bankRegisterRepo: IRepository<BankRegister>,
    private val accountRepo: IRepository<Account>,
): IUseCase<QueryFilter, ListOutput<GetBankRegisterOutput>> {
    override fun execAsync(input: QueryFilter): ListOutput<GetBankRegisterOutput> {
        val res = bankRegisterRepo.getAll(input)
        val accounts = accountRepo.getManyByIds(res.items.flatMap { it.accountslinked.map { acc -> acc.accountId } }.toSet())

        return ListOutput(
            res.items.map {
                GetBankRegisterOutput(
                    id = it.id,
                    title = it.title,
                    accessCode = it.accessCode,
                    cursor = it.cursor,
                    isActive = it.isActive,
                    accounts = it.accountslinked.map { accLink ->
                        val accountName = accounts.first { acc -> acc.id == accLink.accountId }.title
                        AccountLinkerOutput(
                            accountId =  accLink.accountId,
                            bankRegisterId = accLink.bankAccountId,
                            accountName = accountName,
                        )
                    }
                )
            },
            res.total
        )
    }
}