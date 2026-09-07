package dev.auguste.agni_api.core.usecases.bank_registers

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.adapters.repositories.QueryExtendBuilder
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryComparator
import dev.auguste.agni_api.core.entities.Account
import dev.auguste.agni_api.core.entities.BankRegister
import dev.auguste.agni_api.core.entities.DomainException
import dev.auguste.agni_api.core.usecases.bank_registers.dto.AccountLinkerOutput
import dev.auguste.agni_api.core.usecases.bank_registers.dto.GetBankRegisterByAccessCodeInput
import dev.auguste.agni_api.core.usecases.bank_registers.dto.GetBankRegisterOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase

class GetBankRegisterByAccess(
    private val bankRegisterRepo: IRepository<BankRegister>,
    private val accountRepo: IRepository<Account>,
): IUseCase<GetBankRegisterByAccessCodeInput, GetBankRegisterOutput> {
    override fun execAsync(input: GetBankRegisterByAccessCodeInput): GetBankRegisterOutput {
        val condition = QueryExtendBuilder<BankRegister>()

        if (input.institutionId.isBlank())
            throw DomainException.Validation.GetBankRegisterAccessCodeEmpty()

        condition.addCondition("institutionId", QueryComparator.Equal, input.institutionId)

        val bankRegisters = bankRegisterRepo.getAll(QueryFilter.queryAll(), condition)

        if (bankRegisters.items.isEmpty())
            throw DomainException.NotFound.BankRegisterCodeAccess(input.institutionId)

        val bankRegister = bankRegisters.items.first()

        val conditionAccounts = QueryExtendBuilder<Account>()
            .addCondition("accountIds", QueryComparator.In, bankRegister.accountslinked.mapNotNull { it.accountId })
        val accounts = accountRepo.getAll(QueryFilter.queryAll(), conditionAccounts)

        return GetBankRegisterOutput(
            id = bankRegister.id,
            institutionId = bankRegister.institutionId,
            title = bankRegister.title,
            accessCode = bankRegister.accessCode,
            cursor = bankRegister.cursor,
            isActive = bankRegister.isActive,
            accounts = bankRegister.accountslinked.map { AccountLinkerOutput(
                accountId = it.accountId,
                accountName = accounts.items.find { acc -> acc.id == it.accountId }?.title ?: "__NULL__",
                bankRegisterId = it.bankAccountId,
                it.bankName
            ) },
        )
    }
}