package usecase_configs

import adapters.IAuthenticateAdapter
import adapters.dto.QueryFilter
import adapters.repositories.IRepository
import adapters.repositories.IUnitOfWork
import domain.entities.Account
import domain.entities.IncomeSource
import domain.entities.ScheduleInvoice
import domain.entities.User
import usecases.dto.CreatedOutput
import usecases.dto.ListOutput
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
import usecases.UseCase
import usecases.income_sources.dto.GetAllIncomeSourceInput
import java.util.UUID

@Configuration
class IncomeSourceConfig {

    @Bean
    fun createIncomeSource(
        incomeSourceRepo: IRepository<IncomeSource>,
        accountRepo: IRepository<Account>,
        createScheduleInvoice: IUseCase<CreateScheduleInvoiceInput, CreatedOutput>,
        unitOfWork: IUnitOfWork,
        auth: IAuthenticateAdapter
    ) : UseCase<CreateIncomeSourceInput, CreatedOutput> {
        return CreateIncomeSource(
            incomeSourceRepo = incomeSourceRepo,
            accountRepo = accountRepo,
            createScheduleInvoice = createScheduleInvoice,
            unitOfWork = unitOfWork,
            auth = auth
        )
    }

    @Bean
    fun updateIncomeSource(
        incomeSourceRepo: IRepository<IncomeSource>,
        accountRepo: IRepository<Account>,
        scheduleInvoiceRepo: IRepository<ScheduleInvoice>,
        updateScheduleInvoice: IUseCase<UpdateScheduleInvoiceInput, Unit>,
        unitOfWork: IUnitOfWork,
        auth: IAuthenticateAdapter
    ) : UseCase<UpdateIncomeSourceInput, Unit> {
        return UpdateIncomeSource(
            incomeSourceRepo = incomeSourceRepo,
            accountRepo = accountRepo,
            updateScheduleInvoice = updateScheduleInvoice,
            scheduleInvoiceRepo = scheduleInvoiceRepo,
            unitOfWork = unitOfWork,
            auth = auth
        )
    }

    @Bean
    fun deleteIncomeSource(
        incomeSourceRepo: IRepository<IncomeSource>,
        scheduleInvoiceRepo: IRepository<ScheduleInvoice>,
        deleteScheduleInvoice: IUseCase<DeleteScheduleInvoiceInput, Unit>,
        unitOfWork: IUnitOfWork,
        auth: IAuthenticateAdapter
    ) : UseCase<DeleteIncomeSourceInput, Unit> {
        return DeleteIncomeSource(
            incomeSourceRepo = incomeSourceRepo,
            scheduleInvoiceRepo = scheduleInvoiceRepo,
            deleteScheduleInvoice = deleteScheduleInvoice,
            unitOfWork = unitOfWork,
            auth = auth
        )
    }

    @Bean
    fun getIncomeSource(
        incomeSourceRepo: IRepository<IncomeSource>,
        auth: IAuthenticateAdapter
    ) : UseCase<UUID, GetIncomeSourceOutput> {
        return GetIncomeSource(
            incomeSourceRepo = incomeSourceRepo,
            auth = auth
        )
    }

    @Bean
    fun getAllIncomeSource(
        incomeSourceRepo: IRepository<IncomeSource>,
        auth: IAuthenticateAdapter
    ) : UseCase<GetAllIncomeSourceInput, ListOutput<GetIncomeSourceOutput>> {
        return GetAllIncomeSource(
            incomeSourceRepo = incomeSourceRepo,
            auth = auth
        )
    }
}