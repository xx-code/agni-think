package dev.auguste.agni_api.core.usecases.currencies

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Currency
import dev.auguste.agni_api.core.usecases.currencies.dto.GetCurrencyOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import java.util.UUID

class GetCurrency(val currencyRepo: IRepository<Currency>): IUseCase<UUID, GetCurrencyOutput> {
    override fun execAsync(input: UUID): GetCurrencyOutput {
        val currency = currencyRepo.get(input) ?: throw Error("Currency not found")

        return GetCurrencyOutput(
            id = currency.id,
            name = currency.name,
            symbol = currency.symbol,
            rateToBase = currency.rateToBase,
            isBase = currency.isBase,
            locale = currency.locale
        )
    }
}