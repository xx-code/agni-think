package dev.auguste.agni_api.infras.usecase_configs

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.adapters.repositories.IUnitOfWork
import dev.auguste.agni_api.core.entities.Account
import dev.auguste.agni_api.core.entities.IncomeSource
import dev.auguste.agni_api.core.entities.ScheduleInvoice
import dev.auguste.agni_api.core.usecases.CreatedOutput
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.income_sources.CreateIncomeSource
import dev.auguste.agni_api.core.usecases.income_sources.DeleteIncomeSource
import dev.auguste.agni_api.core.usecases.income_sources.GetAllIncomeSource
import dev.auguste.agni_api.core.usecases.income_sources.GetIncomeSource
import dev.auguste.agni_api.core.usecases.income_sources.UpdateIncomeSource
import dev.auguste.agni_api.core.usecases.income_sources.dto.CreateIncomeSourceInput
import dev.auguste.agni_api.core.usecases.income_sources.dto.DeleteIncomeSourceInput
import dev.auguste.agni_api.core.usecases.income_sources.dto.GetIncomeSourceOutput
import dev.auguste.agni_api.core.usecases.income_sources.dto.UpdateIncomeSourceInput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.schedule_Invoices.dto.CreateScheduleInvoiceInput
import dev.auguste.agni_api.core.usecases.schedule_Invoices.dto.DeleteScheduleInvoiceInput
import dev.auguste.agni_api.core.usecases.schedule_Invoices.dto.UpdateScheduleInvoiceInput
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