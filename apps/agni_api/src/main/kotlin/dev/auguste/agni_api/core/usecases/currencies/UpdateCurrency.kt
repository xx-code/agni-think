package dev.auguste.agni_api.core.usecases.currencies

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Currency
import dev.auguste.agni_api.core.usecases.currencies.dto.UpdateCurrencyInput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase

class UpdateCurrency(private val currencyRepo: IRepository<Currency>): IUseCase<UpdateCurrencyInput, Unit> {

    override fun execAsync(input: UpdateCurrencyInput) {
        val currency = currencyRepo.get(input.id) ?: throw Error("Currency ${input.id} not found")

        if (input.name != null) {
            if (input.name != currency.name && currencyRepo.existsByName(input.name))
                throw Error("Currency ${input.name} already exists")

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