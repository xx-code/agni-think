package dev.auguste.agni_api.core.usecases.currencies

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.entities.Currency
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.currencies.dto.GetCurrencyOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase

class GetAllCurrencies(val currencyRepo: IRepository<Currency>): IUseCase<QueryFilter, ListOutput<GetCurrencyOutput>> {

    override fun execAsync(input: QueryFilter): ListOutput<GetCurrencyOutput> {
        val currencies = currencyRepo.getAll(input)

        return ListOutput(
            items = currencies.items.map {
                GetCurrencyOutput(
                    id = it.id,
                    name = it.name,
                    symbol = it.symbol,
                    rateToBase = it.rateToBase,
                    isBase = it.isBase,
                    locale = it.locale
                )
            },
            total = currencies.total
        )

    }
}