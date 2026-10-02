package usecases.currencies

import adapters.repositories.IRepository
import adapters.dto.QueryFilter
import domain.entities.Currency
import usecases.ListOutput
import usecases.currencies.dto.GetCurrencyOutput
import usecases.interfaces.IUseCase

class GetAllCurrencies(private val currencyRepo: IRepository<Currency>): IUseCase<QueryFilter, ListOutput<GetCurrencyOutput>> {

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