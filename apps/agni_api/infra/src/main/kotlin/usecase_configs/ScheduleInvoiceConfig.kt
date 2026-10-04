package usecase_configs

import adapters.dto.QueryFilter
import adapters.events.IEventRegister
import adapters.repositories.IRepository
import adapters.repositories.IUnitOfWork
import domain.entities.IncomeSource
import domain.entities.Provision
import domain.entities.Fund
import domain.entities.ScheduleInvoice
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
        ): UseCase<ScheduleInvoiceModuleLinker, Unit> {
        return VerifyScheduleModuleLinker(
            incomeSourceRepo = incomeSourceRepo,
            fundSourceRepo = fundsSourceRepo,
            provisionRepo = provisionRepo,
        )
    }

    @Bean
    fun createScheduleInvoice(
        scheduleInvoiceRepo: IRepository<ScheduleInvoice>,
        invoiceDependencies: InvoiceDependencies,
        verifyScheduleModuleLinker: IUseCase<ScheduleInvoiceModuleLinker, Unit>,
    ): UseCase<CreateScheduleInvoiceInput, CreatedOutput> {
        return CreateScheduleInvoice(
            scheduleInvoiceRepo = scheduleInvoiceRepo,
            invoiceDependencies = invoiceDependencies,
            verifyScheduleModuleLinker = verifyScheduleModuleLinker,
        )
    }

    @Bean
    fun deleteScheduleInvoice(
        scheduleInvoiceRepo: IRepository<ScheduleInvoice>,
    ): UseCase<DeleteScheduleInvoiceInput, Unit> {
        return DeleteScheduleInvoice(
            scheduleInvoiceRepo = scheduleInvoiceRepo,
        )
    }

    @Bean
    fun getAllScheduleInvoice(
        scheduleInvoiceRepo: IRepository<ScheduleInvoice>,
    ): UseCase<QueryFilter, ListOutput<GetScheduleInvoiceOutput>> {
        return GetAllScheduleInvoice(
            scheduleInvoiceRepo = scheduleInvoiceRepo
        )
    }

    @Bean
    fun getScheduleInvoice(
        scheduleInvoiceRepo: IRepository<ScheduleInvoice>,
    ): UseCase<UUID, GetScheduleInvoiceOutput> {
       return GetScheduleInvoice(
           scheduleInvoiceRepo = scheduleInvoiceRepo
       ) 
    }

    @Bean
    fun updateScheduleInvoice(
        scheduleInvoiceRepo: IRepository<ScheduleInvoice>,
        invoiceDependencies: InvoiceDependencies,
        verifyScheduleModuleLinker: IUseCase<ScheduleInvoiceModuleLinker, Unit>,
    ): UseCase<UpdateScheduleInvoiceInput, Unit> {
        return UpdateScheduleInvoice(
            scheduleInvoiceRepo = scheduleInvoiceRepo,
            invoiceDependencies = invoiceDependencies,
            verifyScheduleModuleLinker = verifyScheduleModuleLinker
        )
    }
}