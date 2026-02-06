package dev.auguste.agni_api.core.usecases.currencies

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Currency
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import java.util.UUID

class DeleteCurrency(val currencyRepo: IRepository<Currency>): IUseCase<UUID, Unit> {

    override fun execAsync(input: UUID) {
        currencyRepo.get(input)?: throw Error("Currency not found")

        currencyRepo.delete(input)
    }
}