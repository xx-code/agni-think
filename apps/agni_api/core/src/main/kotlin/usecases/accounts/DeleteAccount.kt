package usecases.accounts

import adapters.IAuthenticateAdapter
import adapters.repositories.IRepository
import domain.entities.Account
import domain.exceptions.NotFoundException
import usecases.UseCase
import usecases.accounts.dto.DeleteAccountInput
import usecases.interfaces.IUseCase

class DeleteAccount(
    private val accountRepo: IRepository<Account>,
    auth: IAuthenticateAdapter
    ): UseCase<DeleteAccountInput, Unit>(auth = auth) {
    override suspend fun process(input: DeleteAccountInput) {
        accountRepo.get(input.accountId) ?: throw NotFoundException.SingleEntity(input.accountId, "account")
        accountRepo.delete(input.accountId)
    }
}