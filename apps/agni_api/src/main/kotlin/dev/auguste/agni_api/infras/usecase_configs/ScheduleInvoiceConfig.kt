package dev.auguste.agni_api.infras.usecase_configs

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.events.IEventRegister
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.ScheduleInvoice
import dev.auguste.agni_api.core.facades.InvoiceDependencies
import dev.auguste.agni_api.core.usecases.BackgroundTaskOut
import dev.auguste.agni_api.core.usecases.CreatedOutput
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.invoices.dto.CreateFreezeInvoiceInput
import dev.auguste.agni_api.core.usecases.invoices.dto.CreateInvoiceInput
import dev.auguste.agni_api.core.usecases.schedule_Invoices.ApplyScheduleInvoice
import dev.auguste.agni_api.core.usecases.schedule_Invoices.CreateScheduleInvoice
import dev.auguste.agni_api.core.usecases.schedule_Invoices.DeleteScheduleInvoice
import dev.auguste.agni_api.core.usecases.schedule_Invoices.GetAllScheduleInvoice
import dev.auguste.agni_api.core.usecases.schedule_Invoices.GetScheduleInvoice
import dev.auguste.agni_api.core.usecases.schedule_Invoices.UpdateScheduleInvoice
import dev.auguste.agni_api.core.usecases.schedule_Invoices.dto.CreateScheduleInvoiceInput
import dev.auguste.agni_api.core.usecases.schedule_Invoices.dto.DeleteScheduleInvoiceInput
import dev.auguste.agni_api.core.usecases.schedule_Invoices.dto.GetScheduleInvoiceOutput
import dev.auguste.agni_api.core.usecases.schedule_Invoices.dto.UpdateScheduleInvoiceInput
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.UUID

@Configuration
class ScheduleInvoiceConfig {

    @Bean
    fun applyScheduleInvoice(
        scheduleInvoiceRepo: IRepository<ScheduleInvoice>,
        createInvoice: IUseCase<CreateInvoiceInput, CreatedOutput>,
        createFreezeInvoice: IUseCase<CreateFreezeInvoiceInput, CreatedOutput>,
        eventManger: IEventRegister
    ): IUseCase<Unit, BackgroundTaskOut> {
        return ApplyScheduleInvoice(
            scheduleInvoiceRepo = scheduleInvoiceRepo,
            createInvoice = createInvoice,
            createFreezeInvoice = createFreezeInvoice,
            eventManager = eventManger
        )
    }

    @Bean
    fun createScheduleInvoice(
        scheduleInvoiceRepo: IRepository<ScheduleInvoice>,
        invoiceDependencies: InvoiceDependencies
    ): IUseCase<CreateScheduleInvoiceInput, CreatedOutput> {
        return CreateScheduleInvoice(
            scheduleInvoiceRepo = scheduleInvoiceRepo,
            invoiceDependencies = invoiceDependencies
        )
    }

    @Bean
    fun deleteScheduleInvoice(
        scheduleInvoiceRepo: IRepository<ScheduleInvoice>,
    ): IUseCase<DeleteScheduleInvoiceInput, Unit> {
        return DeleteScheduleInvoice(
            scheduleInvoiceRepo = scheduleInvoiceRepo,
        )
    }

    @Bean
    fun getAllScheduleInvoice(
        scheduleInvoiceRepo: IRepository<ScheduleInvoice>,
    ): IUseCase<QueryFilter, ListOutput<GetScheduleInvoiceOutput>> {
        return GetAllScheduleInvoice(
            scheduleInvoiceRepo = scheduleInvoiceRepo
        )
    }

    @Bean
    fun getScheduleInvoice(
        scheduleInvoiceRepo: IRepository<ScheduleInvoice>,
    ): IUseCase<UUID, GetScheduleInvoiceOutput> {
       return GetScheduleInvoice(
           scheduleInvoiceRepo = scheduleInvoiceRepo
       ) 
    }

    @Bean
    fun updateScheduleInvoice(
        scheduleInvoiceRepo: IRepository<ScheduleInvoice>,
        invoiceDependencies: InvoiceDependencies,
    ): IUseCase<UpdateScheduleInvoiceInput, Unit> {
        return UpdateScheduleInvoice(
            scheduleInvoiceRepo = scheduleInvoiceRepo,
            invoiceDependencies = invoiceDependencies
        )
    }
}