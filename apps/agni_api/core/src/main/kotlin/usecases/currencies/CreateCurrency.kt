package usecases.currencies

import usecases.UseCase
import adapters.repositories.IRepository
import domain.entities.Currency
import domain.exceptions.AlreadyExistException
import usecases.dto.CreatedOutput
import usecases.currencies.dto.CreateCurrencyInput
class CreateCurrency(private val currencyRepo: IRepository<Currency>): UseCase<CreateCurrencyInput, CreatedOutput>() {

    override suspend fun process(input: CreateCurrencyInput): CreatedOutput {
        if (currencyRepo.existsByName(input.name))
            throw AlreadyExistException.EntitiesByField(mapOf("name" to input.name), "currency")

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