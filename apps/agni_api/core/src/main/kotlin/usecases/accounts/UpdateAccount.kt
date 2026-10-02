package usecases.accounts

import adapters.repositories.IRepository
import domain.entities.Account
import domain.entities.Color
import domain.exceptions.AlreadyExistException
import domain.exceptions.NotFoundException
import usecases.accounts.dto.UpdateAccountInput
import usecases.interfaces.IUseCase

class UpdateAccount(private val accountRepo: IRepository<Account>): IUseCase<UpdateAccountInput, Unit> {

    override fun execAsync(input: UpdateAccountInput) {
        val account = accountRepo.get(input.id) ?: throw NotFoundException.SingleEntity(input.id, "account")

        if (input.title != null) {
            if (input.title != account.title && accountRepo.existsByName(input.title))
                throw AlreadyExistException.EntitiesByField(mapOf("name" to input.title), "account")

            account.title = input.title
        }

        if (input.detail != null) {
            account.detail = input.detail
        }

        if (input.color != null) {
            val color = Color(input.color)
            if (account.color != color)
                account.color = color
        }

        if (account.hasChanged())
            accountRepo.update(account)
    }
}