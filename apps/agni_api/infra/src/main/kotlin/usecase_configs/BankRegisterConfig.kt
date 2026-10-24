package usecase_configs

import adapters.IAuthenticateAdapter
import adapters.IBankingHandlerAdapter
import adapters.dto.QueryFilter
import adapters.events.IEventRegister
import adapters.repositories.IRepository
import domain.entities.Account
import domain.entities.BankRegister
import domain.entities.ExternalTransaction
import domain.entities.User
import usecases.dto.CreatedOutput
import usecases.dto.ListOutput
import usecases.bank_registers.CreateBankRegister
import usecases.bank_registers.DeleteBankRegister
import usecases.bank_registers.GetAllBankRegisters
import usecases.bank_registers.GetBankRegisterByAccess
import usecases.bank_registers.UpdateBankRegister
import usecases.bank_registers.dto.CreateBankRegisterInput
import usecases.bank_registers.dto.DeleteBankRegisterInput
import usecases.bank_registers.dto.GetBankRegisterByAccessCodeInput
import usecases.bank_registers.dto.GetBankRegisterOutput
import usecases.bank_registers.dto.UpdateBankRegisterInput
import usecases.interfaces.IUseCase
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import usecases.UseCase
import usecases.bank_registers.SyncBankTransaction
import usecases.bank_registers.SyncBankTransactionByBankRegister
import usecases.bank_registers.SyncBankTransactionTask
import usecases.bank_registers.dto.GetAllBankRegisterInput
import usecases.bank_registers.dto.SyncBankTransactionInput
import usecases.dto.BackgroundTaskOut
import java.util.UUID

@Configuration
class BankRegisterConfig {
    @Bean
    fun createBankRegister(
        bankRegisterRepo: IRepository<BankRegister>,
        accountRepo: IRepository<Account>,
        auth: IAuthenticateAdapter
    ): UseCase<CreateBankRegisterInput, CreatedOutput> {
        return CreateBankRegister(
            bankRegisterRepo,
            accountRepo = accountRepo,
            auth = auth
        )
    }

    @Bean
    fun deleteBankRegister(
        bankRegisterRepo: IRepository<BankRegister>,
        auth: IAuthenticateAdapter
    ): IUseCase<DeleteBankRegisterInput, Unit>  {
        return DeleteBankRegister(bankRegisterRepo, auth = auth)
    }

    @Bean
    fun getAllBankRegister(
        bankRegisterRepo: IRepository<BankRegister>,
        accountRepo: IRepository<Account>,
        auth: IAuthenticateAdapter
    ): UseCase<GetAllBankRegisterInput, ListOutput<GetBankRegisterOutput>> {
        return GetAllBankRegisters(
            bankRegisterRepo,
            accountRepo,
            auth = auth
        )
    }

    @Bean
    fun getBankRegisterByAccessCode(
        accountRepo: IRepository<Account>,
        bankRegisterRepo: IRepository<BankRegister>,
        auth: IAuthenticateAdapter
    ): UseCase<GetBankRegisterByAccessCodeInput, GetBankRegisterOutput> {
        return GetBankRegisterByAccess(
            bankRegisterRepo,
            accountRepo,
            auth = auth
        )
    }

    @Bean
    fun updateBankRegister(
        bankRegisterRepo: IRepository<BankRegister>,
        accountRepo: IRepository<Account>,
        auth: IAuthenticateAdapter
    ) : UseCase<UpdateBankRegisterInput, Unit> {
        return UpdateBankRegister(bankRegisterRepo, accountRepo, auth = auth)
    }

    @Bean
    fun syncBankTransactions(
        bankRegisterRepo: IRepository<BankRegister>,
        bankingHandlerAdapter: IBankingHandlerAdapter,
    ): UseCase<SyncBankTransactionInput, Unit> {
        return SyncBankTransaction(
            bankRegisterRepo = bankRegisterRepo,
            bankingHandlerAdapter = bankingHandlerAdapter,
        )
    }

    @Bean
    fun syncBankTransactionsByBankRegister(
        bankRegisterRepo: IRepository<BankRegister>,
        bankingHandlerAdapter: IBankingHandlerAdapter,
        auth: IAuthenticateAdapter
    ): UseCase<UUID, Unit> {
        return SyncBankTransactionByBankRegister(
            bankRegisterRepo = bankRegisterRepo,
            bankingHandlerAdapter = bankingHandlerAdapter,
            auth = auth
        )
    }

    @Bean
    fun syncBankTransactionsTask(
        syncBankTransaction: IUseCase<SyncBankTransactionInput, Unit>,
        eventRegister: IEventRegister
    ): UseCase<Unit, BackgroundTaskOut> {
        return SyncBankTransactionTask(
            syncBankTransactions = syncBankTransaction,
            eventRegister = eventRegister
        )
    }
}