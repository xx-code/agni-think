package usecase_configs

import adapters.IAuthenticateAdapter
import adapters.dto.QueryFilter
import adapters.repositories.IRepository
import domain.entities.Account
import domain.entities.Currency
import domain.entities.Fund
import domain.entities.Profile
import domain.entities.ScheduleInvoice
import domain.entities.User
import usecases.dto.CreatedOutput
import usecases.dto.ListOutput
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
import usecases.UseCase
import usecases.accounts.GetTotalBalanceAmount
import usecases.accounts.dto.GetAllAccountInput
import usecases.accounts.dto.GetTotalBalanceAmountOutput
import usecases.analystics.dto.ForcastSpendingInput
import usecases.analystics.dto.ForcastSpendingOutput
import java.util.UUID

@Configuration
class AccountConfig {

    @Bean
    fun createAccount(
        accountRepo: IRepository<Account>,
        currencyRepo: IRepository<Currency>,
        auth: IAuthenticateAdapter
    ): UseCase<CreateAccountInput, CreatedOutput> {
        return CreateAccount(
            accountRepository = accountRepo,
            currencyRepository = currencyRepo,
            auth = auth
        )
    }

    @Bean
    fun updateAccount(
        accountRepo: IRepository<Account>,
        auth: IAuthenticateAdapter
    ): UseCase<UpdateAccountInput, Unit> {
        return UpdateAccount(
            accountRepo = accountRepo,
            auth = auth
        )
    }

    @Bean
    fun getAccount(
        accountRepo: IRepository<Account>,
        auth: IAuthenticateAdapter
    ): UseCase<UUID, GetAccountOutput> {
        return GetAccount(
            accountRepo = accountRepo,
            auth = auth
        )
    }

    @Bean
    fun getAllAccounts(
        accountRepo: IRepository<Account>,
        auth: IAuthenticateAdapter
    ): UseCase<GetAllAccountInput, ListOutput<GetAccountOutput>> {
        return GetAllAccounts(
            accountRepo = accountRepo,
            auth = auth
        )
    }

    @Bean
    fun getAccountWithDetail(
        accountRepo: IRepository<Account>,
        fundRepo: IRepository<Fund>,
        getBalance: UseCase<GetBalanceInput, GetBalanceOutput>,
        auth: IAuthenticateAdapter
    ): UseCase<UUID, GetAccountWithDetailOutput> {
        return GetAccountWithDetail(
            accountRepo = accountRepo,
            fundRepo = fundRepo,
            getBalance = getBalance,
            auth = auth
        )
    }

    @Bean
    fun getAllAccountsWithDetail(
        accountRepo: IRepository<Account>,
        fundRepo: IRepository<Fund>,
        getBalance: UseCase<GetBalanceInput, GetBalanceOutput>,
        auth: IAuthenticateAdapter
    ) : UseCase<GetAllAccountInput, ListOutput<GetAccountWithDetailOutput>> {
        return GetAllAccountWithDetail(
            accountRepo = accountRepo,
            fundRepo = fundRepo,
            auth = auth,
            getBalance = getBalance
        )
    }

    @Bean
    fun deleteAccount(
        accountRepo: IRepository<Account>,
        auth: IAuthenticateAdapter
    ) : UseCase<DeleteAccountInput, Unit> {
       return DeleteAccount(
           accountRepo = accountRepo,
           auth = auth
       )
    }

    @Bean
    fun getTotalAccountBalance(
        accountRepo: IRepository<Account>,
        fundRepo: IRepository<Fund>,
        profileRepo: IRepository<Profile>,
        scheduleInvoiceRepo: IRepository<ScheduleInvoice>,
        getBalance: UseCase<GetBalanceInput, GetBalanceOutput>,
        forcastSpending: UseCase<ForcastSpendingInput, ForcastSpendingOutput>,
        auth: IAuthenticateAdapter
    ) : UseCase<Unit, GetTotalBalanceAmountOutput> {
        return GetTotalBalanceAmount(
            accountRepo = accountRepo,
            fundRepo = fundRepo,
            profileRepo = profileRepo,
            scheduleInvoiceRepo = scheduleInvoiceRepo,
            getBalance = getBalance,
            forcastSpending = forcastSpending,
            auth = auth,
        )
    }
}