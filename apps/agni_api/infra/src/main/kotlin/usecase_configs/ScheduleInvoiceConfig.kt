package usecase_configs

import adapters.IAuthenticateAdapter
import adapters.dto.QueryFilter
import adapters.events.IEventRegister
import adapters.repositories.IRepository
import adapters.repositories.IUnitOfWork
import domain.entities.IncomeSource
import domain.entities.Provision
import domain.entities.Fund
import domain.entities.ScheduleInvoice
import domain.entities.User
import facades.InvoiceDependencies
import usecases.dto.BackgroundTaskOut
import usecases.dto.CreatedOutput
import usecases.dto.ListOutput
import usecases.interfaces.IUseCase
import usecases.invoices.dto.CreateFreezeInvoiceInput
import usecases.invoices.dto.CreateInvoiceInput
import usecases.schedule_Invoices.ApplyScheduleInvoice
import usecases.schedule_Invoices.CreateScheduleInvoice
import usecases.schedule_Invoices.DeleteScheduleInvoice
import usecases.schedule_Invoices.GetAllScheduleInvoice
import usecases.schedule_Invoices.GetScheduleInvoice
import usecases.schedule_Invoices.UpdateScheduleInvoice
import usecases.schedule_Invoices.VerifyScheduleModuleLinker
import usecases.schedule_Invoices.dto.CreateScheduleInvoiceInput
import usecases.schedule_Invoices.dto.DeleteScheduleInvoiceInput
import usecases.schedule_Invoices.dto.GetScheduleInvoiceOutput
import usecases.schedule_Invoices.dto.UpdateScheduleInvoiceInput
import domain.value_objects.ScheduleInvoiceModuleLinker
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import usecases.UseCase
import usecases.schedule_Invoices.dto.GetAllScheduleInvoiceInput
import java.util.UUID

@Configuration
class ScheduleInvoiceConfig {

    @Bean
    fun applyScheduleInvoice(
        scheduleInvoiceRepo: IRepository<ScheduleInvoice>,
        createInvoice: IUseCase<CreateInvoiceInput, CreatedOutput>,
        createFreezeInvoice: IUseCase<CreateFreezeInvoiceInput, CreatedOutput>,
        eventManger: IEventRegister,
        unitOfWork: IUnitOfWork,
    ): UseCase<Unit, BackgroundTaskOut> {
        return ApplyScheduleInvoice(
            scheduleInvoiceRepo = scheduleInvoiceRepo,
            createInvoice = createInvoice,
            createFreezeInvoice = createFreezeInvoice,
            eventManager = eventManger,
            unitOfWork = unitOfWork
        )
    }

    @Bean
    fun verifyScheduleModuleLinker(
        provisionRepo: IRepository<Provision>,
        incomeSourceRepo: IRepository<IncomeSource>,
        fundsSourceRepo: IRepository<Fund>,
        auth: IAuthenticateAdapter
        ): UseCase<ScheduleInvoiceModuleLinker, Unit> {
        return VerifyScheduleModuleLinker(
            incomeSourceRepo = incomeSourceRepo,
            fundSourceRepo = fundsSourceRepo,
            provisionRepo = provisionRepo,
            auth = auth,
        )
    }

    @Bean
    fun createScheduleInvoice(
        scheduleInvoiceRepo: IRepository<ScheduleInvoice>,
        invoiceDependencies: InvoiceDependencies,
        verifyScheduleModuleLinker: IUseCase<ScheduleInvoiceModuleLinker, Unit>,
        auth: IAuthenticateAdapter
    ): UseCase<CreateScheduleInvoiceInput, CreatedOutput> {
        return CreateScheduleInvoice(
            scheduleInvoiceRepo = scheduleInvoiceRepo,
            invoiceDependencies = invoiceDependencies,
            verifyScheduleModuleLinker = verifyScheduleModuleLinker,
            auth = auth,
        )
    }

    @Bean
    fun deleteScheduleInvoice(
        scheduleInvoiceRepo: IRepository<ScheduleInvoice>,
        auth: IAuthenticateAdapter
    ): UseCase<DeleteScheduleInvoiceInput, Unit> {
        return DeleteScheduleInvoice(
            scheduleInvoiceRepo = scheduleInvoiceRepo,
            auth = auth
        )
    }

    @Bean
    fun getAllScheduleInvoice(
        scheduleInvoiceRepo: IRepository<ScheduleInvoice>,
        auth: IAuthenticateAdapter
    ): UseCase<GetAllScheduleInvoiceInput, ListOutput<GetScheduleInvoiceOutput>> {
        return GetAllScheduleInvoice(
            scheduleInvoiceRepo = scheduleInvoiceRepo,
            auth = auth
        )
    }

    @Bean
    fun getScheduleInvoice(
        scheduleInvoiceRepo: IRepository<ScheduleInvoice>,
        auth: IAuthenticateAdapter
    ): UseCase<UUID, GetScheduleInvoiceOutput> {
       return GetScheduleInvoice(
           scheduleInvoiceRepo = scheduleInvoiceRepo,
           auth = auth
       ) 
    }

    @Bean
    fun updateScheduleInvoice(
        scheduleInvoiceRepo: IRepository<ScheduleInvoice>,
        invoiceDependencies: InvoiceDependencies,
        verifyScheduleModuleLinker: IUseCase<ScheduleInvoiceModuleLinker, Unit>,
        auth: IAuthenticateAdapter
    ): UseCase<UpdateScheduleInvoiceInput, Unit> {
        return UpdateScheduleInvoice(
            scheduleInvoiceRepo = scheduleInvoiceRepo,
            invoiceDependencies = invoiceDependencies,
            verifyScheduleModuleLinker = verifyScheduleModuleLinker,
            auth = auth
        )
    }
}