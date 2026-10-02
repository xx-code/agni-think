package usecases.currencies

import adapters.repositories.IRepository
import domain.entities.Currency
import usecases.currencies.dto.DeleteCurrencyInput
import usecases.interfaces.IUseCase
import domain.exceptions.NotFoundException

class DeleteCurrency(private val currencyRepo: IRepository<Currency>): IUseCase<DeleteCurrencyInput, Unit> {

    override fun execAsync(input: DeleteCurrencyInput) {
        currencyRepo.get(input.currencyId)?: throw NotFoundException.SingleEntity(input.currencyId, "currency")

        currencyRepo.delete(input.currencyId)
    }
}