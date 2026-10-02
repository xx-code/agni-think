package usecases.accounts

import adapters.repositories.IRepository
import domain.entities.Account
import domain.exceptions.NotFoundException
import usecases.accounts.dto.GetAccountOutput
import usecases.interfaces.IUseCase
import java.util.UUID

class GetAccount(private val accountRepo: IRepository<Account>): IUseCase<UUID, GetAccountOutput>{

    override fun execAsync(input: UUID): GetAccountOutput {
        val account = accountRepo.get(input) ?: throw NotFoundException.SingleEntity(input, "account")

        return GetAccountOutput(
            id = account.id,
            title = account.title,
            balance = account.balance,
            type = account.detail.getType().value,
            color = account.color.toString()
        )
    }
}