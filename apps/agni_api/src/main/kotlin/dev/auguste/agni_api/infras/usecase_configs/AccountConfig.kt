package dev.auguste.agni_api.infras.usecase_configs

import adapters.dto.QueryFilter
import adapters.repositories.IRepository
import domain.entities.Account
import domain.entities.Currency
import domain.entities.InternalLoan
import domain.entities.Fund
import usecases.CreatedOutput
import usecases.ListOutput
import usecases.accounts.CreateAccount
import usecases.accounts.DeleteAccount
import usecases.accounts.GetAccount
import usecases.accounts.GetAccountWithDetail
import usecases.accounts.GetAllAccountWithDetail
import usecases.accounts.GetAllAccounts
import usecases.accounts.UpdateAccount
import usecases.accounts.dto.CreateAccountInput
import usecases.accounts.dto.DeleteAccountInput
import usecases.accounts.dto.GetAccountOutput
import usecases.accounts.dto.GetAccountWithDetailOutput
import usecases.accounts.dto.UpdateAccountInput
import usecases.interfaces.IUseCase
import usecases.invoices.dto.GetBalanceInput
import usecases.invoices.dto.GetBalanceOutput
import usecases.invoices.dto.GetInvoiceOutput
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
        fundRepo: IRepository<Fund>,
        internalLoanRepo: IRepository<InternalLoan>,
        getInvoice: IUseCase<UUID, GetInvoiceOutput>,
        getBalance: IUseCase<GetBalanceInput, GetBalanceOutput>
    ): IUseCase<UUID, GetAccountWithDetailOutput> {
        return GetAccountWithDetail(
            accountRepo = accountRepo,
            fundRepo = fundRepo,
            internalLoanRepo = internalLoanRepo,
            getInvoice = getInvoice,
            getBalance = getBalance
        )
    }

    @Bean
    fun getAllAccountsWithDetail(
        accountRepo: IRepository<Account>,
        fundRepo: IRepository<Fund>,
        internalLoanRepo: IRepository<InternalLoan>,
        getInvoice: IUseCase<UUID, GetInvoiceOutput>,
        getBalance: IUseCase<GetBalanceInput, GetBalanceOutput>
    ) : IUseCase<QueryFilter, ListOutput<GetAccountWithDetailOutput>> {
        return GetAllAccountWithDetail(
            accountRepo = accountRepo,
            fundRepo = fundRepo,
            internalLoanRepo = internalLoanRepo,
            getInvoice = getInvoice,
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