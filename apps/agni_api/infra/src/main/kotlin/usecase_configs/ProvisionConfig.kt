package usecase_configs

import adapters.dto.QueryFilter
import adapters.events.IEventRegister
import adapters.repositories.IRepository
import adapters.repositories.IUnitOfWork
import domain.entities.Provision
import domain.entities.Fund
import domain.entities.ScheduleInvoice
import usecases.dto.BackgroundTaskOut
import usecases.dto.CreatedOutput
import usecases.dto.ListOutput
import usecases.interfaces.IUseCase
import usecases.provision.CreateProvision
import usecases.provision.DeleteProvision
import usecases.provision.GetAllProvision
import usecases.provision.GetProvision
import usecases.provision.MakePaymentInstallment
import usecases.provision.UpdateProvision
import usecases.provision.dto.CreateProvisionInput
import usecases.provision.dto.DeleteProvisionInput
import usecases.provision.dto.GetProvisionOutput
import usecases.provision.dto.UpdateProvisionInput
import usecases.funds.dto.DecreaseSavingGoalInput
import usecases.schedule_Invoices.dto.CreateScheduleInvoiceInput
import usecases.schedule_Invoices.dto.DeleteScheduleInvoiceInput
import usecases.schedule_Invoices.dto.UpdateScheduleInvoiceInput
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import usecases.UseCase
import java.util.UUID

@Configuration
class ProvisionConfig {

    @Bean
    fun createProvision(
        provisionRepo: IRepository<Provision>,
        fundRepo: IRepository<Fund>,
        createSchedulerInvoice: IUseCase<CreateScheduleInvoiceInput, CreatedOutput>,
        unitOfWork: IUnitOfWork
    ): UseCase<CreateProvisionInput, CreatedOutput> {
        return CreateProvision(
            provisionRepo = provisionRepo,
            fundRepo = fundRepo,
            createScheduleInvoice = createSchedulerInvoice,
            unitOfWork = unitOfWork
        )
    }

    @Bean
    fun updateProvision(
        unitOfWork: IUnitOfWork,
        provisionRepo: IRepository<Provision>,
        fundRepo: IRepository<Fund>,
        scheduleInvoiceRepo: IRepository<ScheduleInvoice>,
        updateSchedulerInvoice: IUseCase<UpdateScheduleInvoiceInput, Unit>,
    ): UseCase<UpdateProvisionInput, Unit> {
        return UpdateProvision(
            unitOfWork = unitOfWork,
            provisionRepo = provisionRepo,
            fundRepo = fundRepo,
            updateScheduleInvoice = updateSchedulerInvoice,
            scheduleInvoiceRepo = scheduleInvoiceRepo
        )
    }

    @Bean
    fun deleteProvision(
        provisionRepo: IRepository<Provision>,
        scheduleInvoiceRepo: IRepository<ScheduleInvoice>,
        deleteScheduleInvoice: IUseCase<DeleteScheduleInvoiceInput, Unit>,
        unitOfWork: IUnitOfWork
    ): UseCase<DeleteProvisionInput, Unit> {
        return DeleteProvision(
            provisionRepo = provisionRepo,
            scheduleInvoiceRepo = scheduleInvoiceRepo,
            deleteScheduleInvoice = deleteScheduleInvoice,
            unitOfWork = unitOfWork
        )
    }

    @Bean
    fun getProvision(
        provisionRepo: IRepository<Provision>
    ): UseCase<UUID, GetProvisionOutput> {
        return GetProvision(
            provisionRepo = provisionRepo
        )
    }

    @Bean
    fun getAllProvision(
        provisionRepo: IRepository<Provision>
    ): UseCase<QueryFilter, ListOutput<GetProvisionOutput>> {
       return GetAllProvision(
           provisionRepo = provisionRepo
       )
    }


    @Bean
    fun makeProvisionInstallment(
        provisionRepo: IRepository<Provision>,
        fundRepo: IRepository<Fund>,
        decreaseFund: IUseCase<DecreaseSavingGoalInput, Unit>,
        eventManager: IEventRegister,
        unitOfWork: IUnitOfWork
    ): UseCase<Unit, BackgroundTaskOut> {
        return MakePaymentInstallment(
            provisionRepo = provisionRepo,
            fundRepo = fundRepo,
            decreaseFund = decreaseFund,
            eventManager = eventManager,
            unitOfWork = unitOfWork
        )
    }
}