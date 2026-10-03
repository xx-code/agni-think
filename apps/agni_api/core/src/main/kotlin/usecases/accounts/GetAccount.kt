package usecases.accounts

import adapters.repositories.IRepository
import domain.entities.Account
import domain.exceptions.NotFoundException
import usecases.UseCase
import usecases.accounts.dto.GetAccountOutput
import usecases.interfaces.IUseCase
import java.util.UUID

class GetAccount(private val accountRepo: IRepository<Account>): UseCase<UUID, GetAccountOutput>(){

    override suspend fun process(input: UUID): GetAccountOutput {
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