package dev.auguste.agni_api.infras.usecase_configs

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.events.IEventRegister
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.adapters.repositories.IUnitOfWork
import dev.auguste.agni_api.core.entities.Provision
import dev.auguste.agni_api.core.entities.SavingGoal
import dev.auguste.agni_api.core.entities.ScheduleInvoice
import dev.auguste.agni_api.core.usecases.BackgroundTaskOut
import dev.auguste.agni_api.core.usecases.CreatedOutput
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.interfaces.IInnerUseCase
import dev.auguste.agni_api.core.usecases.interfaces.ISuspendableUseCase
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.invoices.dto.CreateFreezeInvoiceInput
import dev.auguste.agni_api.core.usecases.invoices.dto.CreateInvoiceInput
import dev.auguste.agni_api.core.usecases.provisionable.CreateProvisionable
import dev.auguste.agni_api.core.usecases.provisionable.DeleteProvisionable
import dev.auguste.agni_api.core.usecases.provisionable.GetAllProvisionable
import dev.auguste.agni_api.core.usecases.provisionable.GetProvision
import dev.auguste.agni_api.core.usecases.provisionable.MakePaymentInstallment
import dev.auguste.agni_api.core.usecases.provisionable.UpdateProvisionable
import dev.auguste.agni_api.core.usecases.provisionable.dto.CreateProvisionInput
import dev.auguste.agni_api.core.usecases.provisionable.dto.DeleteProvisionInput
import dev.auguste.agni_api.core.usecases.provisionable.dto.GetProvisionOutput
import dev.auguste.agni_api.core.usecases.provisionable.dto.UpdateProvisionInput
import dev.auguste.agni_api.core.usecases.saving_goals.dto.DecreaseSavingGoalInput
import dev.auguste.agni_api.core.usecases.schedule_Invoices.ApplyScheduleInvoice
import dev.auguste.agni_api.core.usecases.schedule_Invoices.dto.CreateScheduleInvoiceInput
import dev.auguste.agni_api.core.usecases.schedule_Invoices.dto.DeleteScheduleInvoiceInput
import dev.auguste.agni_api.core.usecases.schedule_Invoices.dto.UpdateScheduleInvoiceInput
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.UUID

@Configuration
class ProvisionConfig {

    @Bean
    fun createProvision(
        provisionRepo: IRepository<Provision>,
        fundRepo: IRepository<SavingGoal>,
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
        fundRepo: IRepository<SavingGoal>,
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
        fundRepo: IRepository<SavingGoal>,
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