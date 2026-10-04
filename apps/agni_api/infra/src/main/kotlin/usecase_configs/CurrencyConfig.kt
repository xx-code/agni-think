package usecase_configs

import adapters.dto.QueryFilter
import adapters.repositories.IRepository
import domain.entities.Currency
import usecases.dto.CreatedOutput
import usecases.dto.ListOutput
import usecases.currencies.CreateCurrency
import usecases.currencies.DeleteCurrency
import usecases.currencies.GetAllCurrencies
import usecases.currencies.GetCurrency
import usecases.currencies.UpdateCurrency
import usecases.currencies.dto.CreateCurrencyInput
import usecases.currencies.dto.DeleteCurrencyInput
import usecases.currencies.dto.GetCurrencyOutput
import usecases.currencies.dto.UpdateCurrencyInput
import usecases.interfaces.IUseCase
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import usecases.UseCase
import java.util.UUID

@Configuration
class CurrencyConfig {

    @Bean
    fun createCurrency(
       currencyRepo: IRepository<Currency>
    ): UseCase<CreateCurrencyInput, CreatedOutput> {
        return CreateCurrency(
            currencyRepo = currencyRepo
        )
    }

    @Bean
    fun deleteCurrency(
        currencyRepo: IRepository<Currency>
    ): UseCase<DeleteCurrencyInput, Unit> {
        return DeleteCurrency(
            currencyRepo = currencyRepo
        )
    }

    @Bean
    fun getCurrency(
        currencyRepo: IRepository<Currency>
    ): UseCase<UUID, GetCurrencyOutput> {
        return GetCurrency(
            currencyRepo = currencyRepo
        )
    }

    @Bean
    fun getAllCurrencies(
        currencyRepo: IRepository<Currency>
    ): UseCase<QueryFilter, ListOutput<GetCurrencyOutput>> {
        return GetAllCurrencies(
            currencyRepo = currencyRepo
        )
    }

    @Bean
    fun updateCurrency(
        currencyRepo: IRepository<Currency>
    ): UseCase<UpdateCurrencyInput, Unit> {
        return UpdateCurrency(
            currencyRepo = currencyRepo
        )
    }
}