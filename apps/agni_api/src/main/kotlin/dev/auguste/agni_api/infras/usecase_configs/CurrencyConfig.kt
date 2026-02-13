package dev.auguste.agni_api.infras.usecase_configs

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Currency
import dev.auguste.agni_api.core.usecases.CreatedOutput
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.currencies.CreateCurrency
import dev.auguste.agni_api.core.usecases.currencies.DeleteCurrency
import dev.auguste.agni_api.core.usecases.currencies.GetAllCurrencies
import dev.auguste.agni_api.core.usecases.currencies.GetCurrency
import dev.auguste.agni_api.core.usecases.currencies.UpdateCurrency
import dev.auguste.agni_api.core.usecases.currencies.dto.CreateCurrencyInput
import dev.auguste.agni_api.core.usecases.currencies.dto.DeleteCurrencyInput
import dev.auguste.agni_api.core.usecases.currencies.dto.GetCurrencyOutput
import dev.auguste.agni_api.core.usecases.currencies.dto.UpdateCurrencyInput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.tags.dto.DeleteTagInput
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.UUID

@Configuration
class CurrencyConfig {

    @Bean
    fun createCurrency(
       currencyRepo: IRepository<Currency>
    ): IUseCase<CreateCurrencyInput, CreatedOutput> {
        return CreateCurrency(
            currencyRepo = currencyRepo
        )
    }

    @Bean
    fun deleteCurrency(
        currencyRepo: IRepository<Currency>
    ): IUseCase<DeleteCurrencyInput, Unit> {
        return DeleteCurrency(
            currencyRepo = currencyRepo
        )
    }

    @Bean
    fun getCurrency(
        currencyRepo: IRepository<Currency>
    ): IUseCase<UUID, GetCurrencyOutput> {
        return GetCurrency(
            currencyRepo = currencyRepo
        )
    }

    @Bean
    fun getAllCurrencies(
        currencyRepo: IRepository<Currency>
    ): IUseCase<QueryFilter, ListOutput<GetCurrencyOutput>> {
        return GetAllCurrencies(
            currencyRepo = currencyRepo
        )
    }

    @Bean
    fun updateCurrency(
        currencyRepo: IRepository<Currency>
    ): IUseCase<UpdateCurrencyInput, Unit> {
        return UpdateCurrency(
            currencyRepo = currencyRepo
        )
    }
}