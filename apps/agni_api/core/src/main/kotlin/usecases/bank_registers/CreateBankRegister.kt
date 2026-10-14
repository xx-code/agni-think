package usecases.bank_registers

import usecases.UseCase
import adapters.IAuthenticateAdapter
import adapters.repositories.IRepository
import domain.entities.Account
import domain.entities.BankRegister
import domain.entities.User
import domain.exceptions.NotFoundException
import usecases.dto.CreatedOutput
import usecases.bank_registers.dto.CreateBankRegisterInput
import domain.value_objects.AccountLinked

class CreateBankRegister(
    private val bankRegisterRepo: IRepository<BankRegister>,
    private val accountRepo: IRepository<Account>,
    auth: IAuthenticateAdapter
): UseCase<CreateBankRegisterInput, CreatedOutput>(auth = auth) {
    override suspend fun process(input: CreateBankRegisterInput): CreatedOutput {
        val currentUser = getCurrentUser()

        val accountIds = input.accounts.mapNotNull { it.accountId }.toSet()
        val accounts = accountRepo.getManyByIds(accountIds)
        if (accountIds.isNotEmpty() && accounts.size != input.accounts.size)
                throw NotFoundException.EntitiesByOtherField(mapOf("ids" to accountIds.joinToString()), "account")
        val newBankRegister = BankRegister(
            userId = currentUser.id,
            institutionId = input.institutionId,
            title = input.title,
            accessCode = input.accessCode,
            accountsLinked = input.accounts.map {
                AccountLinked(
                    it.accountId,
                    it.bankAccountId,
                    it.bankName
                )
            }.toSet(),
        )

        bankRegisterRepo.create(newBankRegister)

        return CreatedOutput(newBankRegister.id)
    }
}