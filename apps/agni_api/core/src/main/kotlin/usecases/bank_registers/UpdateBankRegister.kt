package usecases.bank_registers

import usecases.UseCase
import adapters.repositories.IRepository
import domain.entities.Account
import domain.entities.BankRegister
import usecases.bank_registers.dto.UpdateBankRegisterInput
import domain.exceptions.NotFoundException
import domain.value_objects.AccountLinked

class UpdateBankRegister(
    private val bankRegisterRepo: IRepository<BankRegister>,
    private val accountRepo: IRepository<Account>,
): UseCase<UpdateBankRegisterInput, Unit>() {
    override suspend fun process(input: UpdateBankRegisterInput) {
        val bankRegister = bankRegisterRepo.get(input.bankRegisterId) ?: throw NotFoundException.SingleEntity(input.bankRegisterId, "bank_register")

        if (!input.accessCode.isNullOrEmpty()) {
           bankRegister.accessCode = input.accessCode
        }

        if (!input.accounts.isNullOrEmpty()) {
            val accountIds = input.accounts.mapNotNull { it.accountId }.toSet()
            val accounts = accountRepo.getManyByIds(accountIds)
            if (accountIds.isNotEmpty() && accounts.size != accountIds.size)
                throw NotFoundException.EntitiesByOtherField(mapOf("ids" to accountIds.joinToString()), "account")

            bankRegister.accountsLinked = input.accounts.map {
                AccountLinked(
                    it.accountId,
                    it.bankAccountId,
                    it.bankName
                )
            }.toSet()
        }

        if (!input.title.isNullOrEmpty()) {
            bankRegister.title = input.title
        }

        if (!input.cursor.isNullOrEmpty())
            bankRegister.cursor = input.cursor

        if (bankRegister.hasChanged())
            bankRegisterRepo.update(bankRegister)
    }
}