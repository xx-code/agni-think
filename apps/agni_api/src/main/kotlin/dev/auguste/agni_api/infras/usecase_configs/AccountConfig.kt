package dev.auguste.agni_api.infras.usecase_configs

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Account
import dev.auguste.agni_api.core.entities.Currency
import dev.auguste.agni_api.core.entities.SavingGoal
import dev.auguste.agni_api.core.usecases.CreatedOutput
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.accounts.CreateAccount
import dev.auguste.agni_api.core.usecases.accounts.DeleteAccount
import dev.auguste.agni_api.core.usecases.accounts.GetAccount
import dev.auguste.agni_api.core.usecases.accounts.GetAccountWithDetail
import dev.auguste.agni_api.core.usecases.accounts.GetAllAccountWithDetail
import dev.auguste.agni_api.core.usecases.accounts.GetAllAccounts
import dev.auguste.agni_api.core.usecases.accounts.UpdateAccount
import dev.auguste.agni_api.core.usecases.accounts.dto.CreateAccountInput
import dev.auguste.agni_api.core.usecases.accounts.dto.DeleteAccountInput
import dev.auguste.agni_api.core.usecases.accounts.dto.GetAccountOutput
import dev.auguste.agni_api.core.usecases.accounts.dto.GetAccountWithDetailOutput
import dev.auguste.agni_api.core.usecases.accounts.dto.UpdateAccountInput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.invoices.dto.GetBalanceInput
import dev.auguste.agni_api.core.usecases.invoices.dto.GetBalanceOutput
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.UUID

@Configuration
class AccountConfig {

    @Bean
    fun createAccount(
        accountRepo: IRepository<Account>,
        currencyRepo: IRepository<Currency>
    ): IUseCase<CreateAccountInput, CreatedOutput> {
        return CreateAccount(
            accountRepository = accountRepo,
            currencyRepository = currencyRepo
        )
    }

    @Bean
    fun updateAccount(
        accountRepo: IRepository<Account>
    ): IUseCase<UpdateAccountInput, Unit> {
        return UpdateAccount(
            accountRepo = accountRepo
        )
    }

    @Bean
    fun getAccount(
        accountRepo: IRepository<Account>,
    ): IUseCase<UUID, GetAccountOutput> {
        return GetAccount(
            accountRepo = accountRepo
        )
    }

    @Bean
    fun getAllAccounts(
        accountRepo: IRepository<Account>
    ): IUseCase<QueryFilter, ListOutput<GetAccountOutput>> {
        return GetAllAccounts(
            accountRepo = accountRepo
        )
    }

    @Bean
    fun getAccountWithDetail(
        accountRepo: IRepository<Account>,
        savingGoalRepo: IRepository<SavingGoal>,
        getBalance: IUseCase<GetBalanceInput, GetBalanceOutput>
    ): IUseCase<UUID, GetAccountWithDetailOutput> {
        return GetAccountWithDetail(
            accountRepo = accountRepo,
            savingGoalRepo = savingGoalRepo,
            getBalance = getBalance
        )
    }

    @Bean
    fun getAllAccountsWithDetail(
        accountRepo: IRepository<Account>,
        savingGoalRepo: IRepository<SavingGoal>,
        getBalance: IUseCase<GetBalanceInput, GetBalanceOutput>
    ) : IUseCase<QueryFilter, ListOutput<GetAccountWithDetailOutput>> {
        return GetAllAccountWithDetail(
            accountRepo = accountRepo,
            savingGoalRepo = savingGoalRepo,
            getBalance = getBalance
        )
    }

    @Bean
    fun deleteAccount(
        accountRepo: IRepository<Account>,
    ) : IUseCase<DeleteAccountInput, Unit> {
       return DeleteAccount(
           accountRepo = accountRepo
       )
    }
}