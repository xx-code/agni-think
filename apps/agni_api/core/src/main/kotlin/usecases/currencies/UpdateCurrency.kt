package usecases.currencies

import adapters.repositories.IRepository
import domain.entities.Currency
import usecases.currencies.dto.UpdateCurrencyInput
import domain.exceptions.AlreadyExistException
import domain.exceptions.NotFoundException
import usecases.interfaces.IUseCase

class UpdateCurrency(private val currencyRepo: IRepository<Currency>): IUseCase<UpdateCurrencyInput, Unit> {

    override fun execAsync(input: UpdateCurrencyInput) {
        val currency = currencyRepo.get(input.id) ?: throw NotFoundException.SingleEntity(input.id, "currency")

        if (input.name != null) {
            if (input.name != currency.name && currencyRepo.existsByName(input.name))
                throw AlreadyExistException.EntitiesByField(mapOf("name" to input.name), "currency")

            currency.name = input.name
        }

        if (input.locale != null)
            currency.locale = input.locale

        if (input.isBase != null)
            currency.isBase = input.isBase

        if (input.rateToBase != null)
            currency.rateToBase = input.rateToBase

        if (currency.hasChanged())
            currencyRepo.update(currency)
    }
}