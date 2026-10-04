package usecases.currencies

import usecases.UseCase
import adapters.repositories.IRepository
import domain.entities.Currency
import usecases.currencies.dto.GetCurrencyOutput
import domain.exceptions.NotFoundException
import java.util.UUID

class GetCurrency(private val currencyRepo: IRepository<Currency>): UseCase<UUID, GetCurrencyOutput>() {
    override suspend fun process(input: UUID): GetCurrencyOutput {
        val currency = currencyRepo.get(input) ?: throw NotFoundException.SingleEntity(input, "currency")

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