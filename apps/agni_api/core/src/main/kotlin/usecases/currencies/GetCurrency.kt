package usecases.currencies

import adapters.repositories.IRepository
import domain.entities.Currency
import usecases.currencies.dto.GetCurrencyOutput
import usecases.interfaces.IUseCase
import domain.exceptions.NotFoundException
import java.util.UUID

class GetCurrency(private val currencyRepo: IRepository<Currency>): IUseCase<UUID, GetCurrencyOutput> {
    override fun execAsync(input: UUID): GetCurrencyOutput {
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