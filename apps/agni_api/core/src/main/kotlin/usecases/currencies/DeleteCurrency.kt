package usecases.currencies

import adapters.IAuthenticateAdapter
import usecases.UseCase
import adapters.repositories.IRepository
import domain.entities.Currency
import usecases.currencies.dto.DeleteCurrencyInput
import domain.exceptions.NotFoundException

class DeleteCurrency(
    private val currencyRepo: IRepository<Currency>,
    auth: IAuthenticateAdapter): UseCase<DeleteCurrencyInput, Unit>(auth = auth) {

    override suspend fun process(input: DeleteCurrencyInput) {
        currencyRepo.get(input.currencyId)?: throw NotFoundException.SingleEntity(input.currencyId, "currency")

        currencyRepo.delete(input.currencyId)
    }
}