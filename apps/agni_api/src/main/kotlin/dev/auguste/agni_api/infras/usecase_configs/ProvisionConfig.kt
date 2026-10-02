package dev.auguste.agni_api.infras.usecase_configs

import adapters.dto.QueryFilter
import adapters.events.IEventRegister
import adapters.repositories.IRepository
import adapters.repositories.IUnitOfWork
import domain.entities.Provision
import domain.entities.Fund
import domain.entities.ScheduleInvoice
import usecases.BackgroundTaskOut
import usecases.CreatedOutput
import usecases.ListOutput
import usecases.interfaces.IInnerUseCase
import usecases.interfaces.ISuspendableUseCase
import usecases.interfaces.IUseCase
import usecases.provisionable.CreateProvisionable
import usecases.provisionable.DeleteProvisionable
import usecases.provisionable.GetAllProvisionable
import usecases.provisionable.GetProvision
import usecases.provisionable.MakePaymentInstallment
import usecases.provisionable.UpdateProvisionable
import usecases.provisionable.dto.CreateProvisionInput
import usecases.provisionable.dto.DeleteProvisionInput
import usecases.provisionable.dto.GetProvisionOutput
import usecases.provisionable.dto.UpdateProvisionInput
import usecases.saving_goals.dto.DecreaseSavingGoalInput
import usecases.schedule_Invoices.dto.CreateScheduleInvoiceInput
import usecases.schedule_Invoices.dto.DeleteScheduleInvoiceInput
import usecases.schedule_Invoices.dto.UpdateScheduleInvoiceInput
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.UUID

@Configuration
class ProvisionConfig {

    @Bean
    fun createProvision(
        provisionRepo: IRepository<Provision>,
        fundRepo: IRepository<Fund>,
        createSchedulerInvoice: IUseCase<CreateScheduleInvoiceInput, CreatedOutput>,
        unitOfWork: IUnitOfWork
    ): IUseCase<CreateProvisionInput, CreatedOutput> {
        return CreateProvisionable(
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
    ): IUseCase<UpdateProvisionInput, Unit> {
        return UpdateProvisionable(
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
    ): IUseCase<DeleteProvisionInput, Unit> {
        return DeleteProvisionable(
            provisionRepo = provisionRepo,
            scheduleInvoiceRepo = scheduleInvoiceRepo,
            deleteScheduleInvoice = deleteScheduleInvoice,
            unitOfWork = unitOfWork
        )
    }

    @Bean
    fun getProvision(
        provisionRepo: IRepository<Provision>
    ): IUseCase<UUID, GetProvisionOutput> {
        return GetProvision(
            provisionRepo = provisionRepo
        )
    }

    @Bean
    fun getAllProvision(
        provisionRepo: IRepository<Provision>
    ): IUseCase<QueryFilter, ListOutput<GetProvisionOutput>> {
       return GetAllProvisionable(
           provisionRepo = provisionRepo
       )
    }


    @Bean
    fun makeProvisionInstallment(
        provisionRepo: IRepository<Provision>,
        fundRepo: IRepository<Fund>,
        decreaseFund: IInnerUseCase<DecreaseSavingGoalInput, Unit>,
        eventManager: IEventRegister,
        unitOfWork: IUnitOfWork
    ): ISuspendableUseCase<Unit, BackgroundTaskOut> {
        return MakePaymentInstallment(
            provisionRepo = provisionRepo,
            fundRepo = fundRepo,
            decreaseFund = decreaseFund,
            eventManager = eventManager,
            unitOfWork = unitOfWork
        )
    }
}