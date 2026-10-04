package usecases.currencies

import usecases.UseCase
import adapters.repositories.IRepository
import domain.entities.Currency
import usecases.currencies.dto.DeleteCurrencyInput
import domain.exceptions.NotFoundException

class DeleteCurrency(private val currencyRepo: IRepository<Currency>): UseCase<DeleteCurrencyInput, Unit>() {

    override suspend fun process(input: DeleteCurrencyInput) {
        currencyRepo.get(input.currencyId)?: throw NotFoundException.SingleEntity(input.currencyId, "currency")

        currencyRepo.delete(input.currencyId)
    }
}