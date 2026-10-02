package dev.auguste.agni_api.infras.usecase_configs

import adapters.dto.QueryFilter
import adapters.repositories.IRepository
import adapters.repositories.IUnitOfWork
import domain.entities.Account
import domain.entities.IncomeSource
import domain.entities.ScheduleInvoice
import usecases.CreatedOutput
import usecases.ListOutput
import usecases.income_sources.CreateIncomeSource
import usecases.income_sources.DeleteIncomeSource
import usecases.income_sources.GetAllIncomeSource
import usecases.income_sources.GetIncomeSource
import usecases.income_sources.UpdateIncomeSource
import usecases.income_sources.dto.CreateIncomeSourceInput
import usecases.income_sources.dto.DeleteIncomeSourceInput
import usecases.income_sources.dto.GetIncomeSourceOutput
import usecases.income_sources.dto.UpdateIncomeSourceInput
import usecases.interfaces.IUseCase
import usecases.schedule_Invoices.dto.CreateScheduleInvoiceInput
import usecases.schedule_Invoices.dto.DeleteScheduleInvoiceInput
import usecases.schedule_Invoices.dto.UpdateScheduleInvoiceInput
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.UUID

@Configuration
class IncomeSourceConfig {

    @Bean
    fun createIncomeSource(
        incomeSourceRepo: IRepository<IncomeSource>,
        accountRepo: IRepository<Account>,
        createScheduleInvoice: IUseCase<CreateScheduleInvoiceInput, CreatedOutput>,
        unitOfWork: IUnitOfWork
    ) : IUseCase<CreateIncomeSourceInput, CreatedOutput> {
        return CreateIncomeSource(
            incomeSourceRepo,
            createScheduleInvoice,
            accountRepo,
            unitOfWork
        )
    }

    @Bean
    fun updateIncomeSource(
        incomeSourceRepo: IRepository<IncomeSource>,
        accountRepo: IRepository<Account>,
        scheduleInvoiceRepo: IRepository<ScheduleInvoice>,
        updateScheduleInvoice: IUseCase<UpdateScheduleInvoiceInput, Unit>,
        unitOfWork: IUnitOfWork
    ) : IUseCase<UpdateIncomeSourceInput, Unit> {
        return UpdateIncomeSource(
            incomeSourceRepo,
            accountRepo,
            updateScheduleInvoice,
            scheduleInvoiceRepo,
            unitOfWork
        )
    }

    @Bean
    fun deleteIncomeSource(
        incomeSourceRepo: IRepository<IncomeSource>,
        scheduleInvoiceRepo: IRepository<ScheduleInvoice>,
        deleteScheduleInvoice: IUseCase<DeleteScheduleInvoiceInput, Unit>,
        unitOfWork: IUnitOfWork
    ) : IUseCase<DeleteIncomeSourceInput, Unit> {
        return DeleteIncomeSource(
            incomeSourceRepo,
            scheduleInvoiceRepo,
            deleteScheduleInvoice,
            unitOfWork
        )
    }

    @Bean
    fun getIncomeSource(
        incomeSourceRepo: IRepository<IncomeSource>,
    ) : IUseCase<UUID, GetIncomeSourceOutput> {
        return GetIncomeSource(
            incomeSourceRepo
        )
    }

    @Bean
    fun getAllIncomeSource(
        incomeSourceRepo: IRepository<IncomeSource>,
    ) : IUseCase<QueryFilter, ListOutput<GetIncomeSourceOutput>> {
        return GetAllIncomeSource(
            incomeSourceRepo
        )
    }
}