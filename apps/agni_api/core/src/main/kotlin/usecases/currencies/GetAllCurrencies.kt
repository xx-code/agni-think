package usecases.currencies

import adapters.IAuthenticateAdapter
import usecases.UseCase
import adapters.repositories.IRepository
import adapters.dto.QueryFilter
import domain.entities.Currency
import usecases.dto.ListOutput
import usecases.currencies.dto.GetCurrencyOutput
class GetAllCurrencies(
    private val currencyRepo: IRepository<Currency>,
    auth: IAuthenticateAdapter): UseCase<QueryFilter, ListOutput<GetCurrencyOutput>>(auth=auth) {

    override suspend fun process(input: QueryFilter): ListOutput<GetCurrencyOutput> {
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