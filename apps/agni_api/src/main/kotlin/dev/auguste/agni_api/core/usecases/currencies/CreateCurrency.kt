package dev.auguste.agni_api.core.usecases.currencies

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Currency
import dev.auguste.agni_api.core.usecases.CreatedOutput
import dev.auguste.agni_api.core.usecases.currencies.dto.CreateCurrencyInput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase

class CreateCurrency(private val currencyRepo: IRepository<Currency>): IUseCase<CreateCurrencyInput, CreatedOutput> {

    override fun execAsync(input: CreateCurrencyInput): CreatedOutput {
        if (currencyRepo.existsByName(input.name))
            throw Error("Currency already exists")

        val newCurrency = Currency(
            name = input.name,
            symbol = input.symbol,
            locale = input.locale,
            rateToBase = input.rateToBase,
            isBase = false
        )

        currencyRepo.create(newCurrency)

        return CreatedOutput(newCurrency.id)
    }
}