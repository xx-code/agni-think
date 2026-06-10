package dev.auguste.agni_api.core.usecases.currencies

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Currency
import dev.auguste.agni_api.core.usecases.currencies.dto.DeleteCurrencyInput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.entities.DomainException
import java.util.UUID

class DeleteCurrency(private val currencyRepo: IRepository<Currency>): IUseCase<DeleteCurrencyInput, Unit> {

    override fun execAsync(input: DeleteCurrencyInput) {
        currencyRepo.get(input.currencyId)?: throw DomainException.NotFound.Currency(input.currencyId.toString())

        currencyRepo.delete(input.currencyId)
    }
}