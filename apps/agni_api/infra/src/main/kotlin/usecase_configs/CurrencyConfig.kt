package usecase_configs

import adapters.IAuthenticateAdapter
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
       currencyRepo: IRepository<Currency>,
       auth: IAuthenticateAdapter
    ): UseCase<CreateCurrencyInput, CreatedOutput> {
        return CreateCurrency(
            currencyRepo = currencyRepo,
            auth = auth,
        )
    }

    @Bean
    fun deleteCurrency(
        currencyRepo: IRepository<Currency>,
        auth: IAuthenticateAdapter
    ): UseCase<DeleteCurrencyInput, Unit> {
        return DeleteCurrency(
            currencyRepo = currencyRepo,
            auth = auth,
        )
    }

    @Bean
    fun getCurrency(
        currencyRepo: IRepository<Currency>,
        auth: IAuthenticateAdapter
    ): UseCase<UUID, GetCurrencyOutput> {
        return GetCurrency(
            currencyRepo = currencyRepo,
            auth = auth,
        )
    }

    @Bean
    fun getAllCurrencies(
        currencyRepo: IRepository<Currency>,
        auth: IAuthenticateAdapter
    ): UseCase<QueryFilter, ListOutput<GetCurrencyOutput>> {
        return GetAllCurrencies(
            currencyRepo = currencyRepo,
            auth = auth,
        )
    }

    @Bean
    fun updateCurrency(
        currencyRepo: IRepository<Currency>,
        auth: IAuthenticateAdapter
    ): UseCase<UpdateCurrencyInput, Unit> {
        return UpdateCurrency(
            currencyRepo = currencyRepo,
            auth = auth,
        )
    }
}