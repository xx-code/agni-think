package usecase_configs

import adapters.IAuthenticateAdapter
import adapters.dto.QueryFilter
import adapters.events.IEventRegister
import adapters.repositories.IRepository
import adapters.repositories.IUnitOfWork
import domain.entities.Provision
import domain.entities.Fund
import domain.entities.ScheduleInvoice
import domain.entities.User
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
import usecases.schedule_Invoices.dto.CreateScheduleInvoiceInput
import usecases.schedule_Invoices.dto.DeleteScheduleInvoiceInput
import usecases.schedule_Invoices.dto.UpdateScheduleInvoiceInput
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import usecases.UseCase
import usecases.funds.dto.DecreaseFundInput
import usecases.provision.dto.GetAllProvisionInput
import java.util.UUID

@Configuration
class ProvisionConfig {

    @Bean
    fun createProvision(
        provisionRepo: IRepository<Provision>,
        fundRepo: IRepository<Fund>,
        createSchedulerInvoice: IUseCase<CreateScheduleInvoiceInput, CreatedOutput>,
        unitOfWork: IUnitOfWork,
        auth: IAuthenticateAdapter
    ): UseCase<CreateProvisionInput, CreatedOutput> {
        return CreateProvision(
            provisionRepo = provisionRepo,
            fundRepo = fundRepo,
            createScheduleInvoice = createSchedulerInvoice,
            unitOfWork = unitOfWork,
            auth = auth
        )
    }

    @Bean
    fun updateProvision(
        unitOfWork: IUnitOfWork,
        provisionRepo: IRepository<Provision>,
        fundRepo: IRepository<Fund>,
        scheduleInvoiceRepo: IRepository<ScheduleInvoice>,
        updateSchedulerInvoice: IUseCase<UpdateScheduleInvoiceInput, Unit>,
        auth: IAuthenticateAdapter,
    ): UseCase<UpdateProvisionInput, Unit> {
        return UpdateProvision(
            unitOfWork = unitOfWork,
            provisionRepo = provisionRepo,
            fundRepo = fundRepo,
            updateScheduleInvoice = updateSchedulerInvoice,
            scheduleInvoiceRepo = scheduleInvoiceRepo,
            auth = auth
        )
    }

    @Bean
    fun deleteProvision(
        provisionRepo: IRepository<Provision>,
        scheduleInvoiceRepo: IRepository<ScheduleInvoice>,
        deleteScheduleInvoice: IUseCase<DeleteScheduleInvoiceInput, Unit>,
        unitOfWork: IUnitOfWork,
        auth: IAuthenticateAdapter
    ): UseCase<DeleteProvisionInput, Unit> {
        return DeleteProvision(
            provisionRepo = provisionRepo,
            scheduleInvoiceRepo = scheduleInvoiceRepo,
            deleteScheduleInvoice = deleteScheduleInvoice,
            unitOfWork = unitOfWork,
            auth = auth
        )
    }

    @Bean
    fun getProvision(
        provisionRepo: IRepository<Provision>,
        auth: IAuthenticateAdapter
    ): UseCase<UUID, GetProvisionOutput> {
        return GetProvision(
            provisionRepo = provisionRepo,
            auth = auth
        )
    }

    @Bean
    fun getAllProvision(
        provisionRepo: IRepository<Provision>,
        auth: IAuthenticateAdapter
    ): UseCase<GetAllProvisionInput, ListOutput<GetProvisionOutput>> {
       return GetAllProvision(
           provisionRepo = provisionRepo,
           auth = auth
       )
    }


    @Bean
    fun makeProvisionInstallment(
        provisionRepo: IRepository<Provision>,
        fundRepo: IRepository<Fund>,
        decreaseFund: IUseCase<DecreaseFundInput, Unit>,
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