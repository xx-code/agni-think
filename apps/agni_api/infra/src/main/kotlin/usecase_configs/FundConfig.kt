package usecase_configs

import adapters.IAuthenticateAdapter
import adapters.events.IEventRegister
import adapters.repositories.IRepository
import adapters.repositories.IUnitOfWork
import domain.entities.Account
import domain.entities.Goal
import domain.entities.Fund
import domain.entities.Invoice
import domain.entities.ScheduleInvoice
import domain.entities.User
import usecases.dto.CreatedOutput
import usecases.dto.ListOutput
import usecases.interfaces.IUseCase
import usecases.invoices.dto.CreateInvoiceInput
import usecases.funds.CreateFund
import usecases.funds.DecreaseFund
import usecases.funds.DeleteFund
import usecases.funds.GetAllFunds
import usecases.funds.GetFund
import usecases.funds.IncreaseFund
import usecases.funds.UpdateFund
import usecases.funds.dto.CreateFundInput
import usecases.funds.dto.DeleteFundInput
import usecases.funds.dto.GetAllFundInput
import usecases.funds.dto.GetFundOutput
import usecases.funds.dto.UpdateFundInput
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import usecases.UseCase
import usecases.dto.BackgroundTaskOut
import usecases.funds.FundAutoCurate
import usecases.funds.UpdateScheduleInvoiceSinkingFund
import usecases.funds.dto.DecreaseFundInput
import usecases.funds.dto.IncreaseFundInput
import usecases.invoices.dto.DeleteInvoiceInput
import usecases.schedule_Invoices.dto.DeleteScheduleInvoiceInput
import java.util.UUID

@Configuration
class FundConfig {

    @Bean
    fun createFund(
        fundRepo: IRepository<Fund>,
        accountRepo: IRepository<Account>,
        scheduleInvoiceRepo: IRepository<ScheduleInvoice>,
        auth: IAuthenticateAdapter,
        uow: IUnitOfWork
    ): UseCase<CreateFundInput, CreatedOutput> {
        return CreateFund(
            fundRepo = fundRepo,
            accountingRepo = accountRepo,
            auth = auth,
            scheduleInvoiceRepo = scheduleInvoiceRepo,
            uow = uow
        )
    }

    @Bean
    fun updateFund(
        fundRepo: IRepository<Fund>,
        accountRepo: IRepository<Account>,
        scheduleInvoiceRepo: IRepository<ScheduleInvoice>,
        auth: IAuthenticateAdapter,
        uow: IUnitOfWork
    ): UseCase<UpdateFundInput, Unit> {
        return UpdateFund(
            fundRepo = fundRepo,
            accountRepo = accountRepo,
            auth = auth,
            scheduleInvoiceRepo = scheduleInvoiceRepo,
            uow = uow
        )
    }

    @Bean
    fun updateFundScheduleInvoice(
       scheduleInvoiceRepo: IRepository<ScheduleInvoice>,
    ): UseCase<Fund, Unit> {
        return UpdateScheduleInvoiceSinkingFund(
            scheduleInvoiceRepo = scheduleInvoiceRepo,
        )
    }

    @Bean
    fun decreaseFund(
        fundRepo: IRepository<Fund>,
        accountRepo: IRepository<Account>,
        createInvoice: IUseCase<CreateInvoiceInput, CreatedOutput>,
        updateFundScheduleInvoice: IUseCase<Fund, Unit>,
        userRepo: IRepository<User>,
        unitOfWork: IUnitOfWork,
    ): UseCase<DecreaseFundInput, Unit> {
        return DecreaseFund(
            fundRepo = fundRepo,
            accountRepo = accountRepo,
            createInvoice = createInvoice,
            unitOfWork = unitOfWork,
            updateFundScheduleInvoice = updateFundScheduleInvoice,
            userRepo = userRepo
        )
    }

    @Bean
    fun increaseFund(
        fundRepo: IRepository<Fund>,
        accountRepo: IRepository<Account>,
        createInvoice: IUseCase<CreateInvoiceInput, CreatedOutput>,
        updateFundScheduleInvoice: IUseCase<Fund, Unit>,
        userRepo: IRepository<User>,
        unitOfWork: IUnitOfWork,
    ): UseCase<IncreaseFundInput, Unit> {
       return IncreaseFund(
           fundRepo = fundRepo,
           accountRepo = accountRepo,
           createInvoice = createInvoice,
           unitOfWork = unitOfWork,
           updateFundScheduleInvoice = updateFundScheduleInvoice,
           userRepo = userRepo
       )
    }

    @Bean
   fun deleteFund(
        fundRepo: IRepository<Fund>,
        accountRepo: IRepository<Account>,
        goalRepo: IRepository<Goal>,
        createInvoice: IUseCase<CreateInvoiceInput, CreatedOutput>,
        scheduleInvoiceRepo: IRepository<ScheduleInvoice>,
        deleteScheduleInvoice: IUseCase<DeleteScheduleInvoiceInput, Unit>,
        invoiceRepo: IRepository<Invoice>,
        unitOfWork: IUnitOfWork,
        auth: IAuthenticateAdapter,
   ): UseCase<DeleteFundInput, Unit> {
       return DeleteFund(
           fundRepo = fundRepo,
           accountRepo = accountRepo,
           createInvoice = createInvoice,
           unitOfWork = unitOfWork,
           goalRepo = goalRepo,
           auth = auth,
           invoiceRepo = invoiceRepo,
           scheduleInvoiceRepo = scheduleInvoiceRepo,
           deleteScheduleInvoice = deleteScheduleInvoice
       )
   }

    @Bean
    fun getAllFund(
        fundRepo: IRepository<Fund>,
        goalRepo: IRepository<Goal>,
        auth: IAuthenticateAdapter
    ): UseCase<GetAllFundInput, ListOutput<GetFundOutput>> {
        return GetAllFunds(
            fundRepo = fundRepo,
            goalRepo = goalRepo,
            auth = auth
        )
    }

    @Bean
    fun getFund(
        fundRepo: IRepository<Fund>,
        goalRepo: IRepository<Goal>,
        auth: IAuthenticateAdapter
    ): UseCase<UUID, GetFundOutput> {
        return GetFund(
            fundRepo = fundRepo,
            goalRepo = goalRepo,
            auth = auth
        )
    }

    @Bean
    fun fundAutoCurate(
        fundRepo: IRepository<Fund>,
        scheduleInvoiceRepo: IRepository<ScheduleInvoice>,
        decreaseFund: UseCase<DecreaseFundInput, Unit>,
        eventRepo: IEventRegister,
        uow: IUnitOfWork
    ): UseCase<Unit, BackgroundTaskOut> {
        return FundAutoCurate(
            fundRepo = fundRepo,
            scheduleInvoiceRepo = scheduleInvoiceRepo,
            decreaseFund = decreaseFund,
            eventRegister = eventRepo,
            uow = uow
        )
    }
}