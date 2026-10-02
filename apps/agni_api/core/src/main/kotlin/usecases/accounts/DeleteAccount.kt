package usecases.accounts

import adapters.repositories.IRepository
import domain.entities.Account
import domain.exceptions.NotFoundException
import usecases.accounts.dto.DeleteAccountInput
import usecases.interfaces.IUseCase

class DeleteAccount(private val accountRepo: IRepository<Account>): IUseCase<DeleteAccountInput, Unit> {
    override fun execAsync(input: DeleteAccountInput) {
        accountRepo.get(input.accountId) ?: throw NotFoundException.SingleEntity(input.accountId, "account")

        accountRepo.delete(input.accountId)
    }
}