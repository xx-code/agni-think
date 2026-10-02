package usecases.bank_registers

import adapters.dto.QueryFilter
import adapters.repositories.IRepository
import adapters.repositories.QueryExtendBuilder
import adapters.repositories.query_extend.QueryComparator
import domain.entities.Account
import domain.entities.BankRegister
import domain.exceptions.NotFoundException
import domain.exceptions.ValidationException
import usecases.bank_registers.dto.AccountLinkerOutput
import usecases.bank_registers.dto.GetBankRegisterByAccessCodeInput
import usecases.bank_registers.dto.GetBankRegisterOutput
import usecases.interfaces.IUseCase

class GetBankRegisterByAccess(
    private val bankRegisterRepo: IRepository<BankRegister>,
    private val accountRepo: IRepository<Account>,
): IUseCase<GetBankRegisterByAccessCodeInput, GetBankRegisterOutput> {
    override fun execAsync(input: GetBankRegisterByAccessCodeInput): GetBankRegisterOutput {
        val condition = QueryExtendBuilder<BankRegister>()

        if (input.institutionId.isBlank())
            throw ValidationException.GetBankRegisterAccessCodeEmpty()

        condition.addCondition("institutionId", QueryComparator.Equal, input.institutionId)

        val bankRegisters = bankRegisterRepo.getAll(QueryFilter.queryAll(), condition)

        if (bankRegisters.items.isEmpty())
            throw NotFoundException.EntityByOtherField("accessCode", input.institutionId, "bank_register_access")

        val bankRegister = bankRegisters.items.first()

        val conditionAccounts = QueryExtendBuilder<Account>()
            .addCondition("accountIds", QueryComparator.In, bankRegister.accountsLinked.mapNotNull { it.accountId })
        val accounts = accountRepo.getAll(QueryFilter.queryAll(), conditionAccounts)

        return GetBankRegisterOutput(
            id = bankRegister.id,
            institutionId = bankRegister.institutionId,
            title = bankRegister.title,
            accessCode = bankRegister.accessCode,
            cursor = bankRegister.cursor,
            isActive = bankRegister.isActive,
            accounts = bankRegister.accountsLinked.map { AccountLinkerOutput(
                accountId = it.accountId,
                accountName = accounts.items.find { acc -> acc.id == it.accountId }?.title ?: "__NULL__",
                bankRegisterId = it.bankAccountId,
                it.bankName
            ) },
        )
    }
}