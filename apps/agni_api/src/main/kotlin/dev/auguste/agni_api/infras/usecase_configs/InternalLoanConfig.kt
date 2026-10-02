package dev.auguste.agni_api.infras.usecase_configs

import adapters.dto.QueryFilter
import adapters.events.IEventRegister
import adapters.repositories.IRepository
import adapters.repositories.IUnitOfWork
import domain.entities.Account
import domain.entities.InternalLoan
import domain.entities.Invoice
import domain.entities.ScheduleInvoice
import usecases.BackgroundTaskOut
import usecases.CreatedOutput
import usecases.ListOutput
import usecases.interfaces.IInnerUseCase
import usecases.interfaces.ISuspendableUseCase
import usecases.interfaces.IUseCase
import usecases.internal_loan.AddRefundInternalLoan
import usecases.internal_loan.AutoCompleteInternalLoan
import usecases.internal_loan.CreateInternalLoan
import usecases.internal_loan.DeleteInternalLoan
import usecases.internal_loan.GetAllInternalLoan
import usecases.internal_loan.GetInternalLoan
import usecases.internal_loan.RemoveRefundInternalLoan
import usecases.internal_loan.UpdateInternalLoan
import usecases.internal_loan.dto.AddRefundInternalLoanInput
import usecases.internal_loan.dto.CreateInternalLoanInput
import usecases.internal_loan.dto.GetInternalLoanOutput
import usecases.internal_loan.dto.RemoveRefundInternalLoanInput
import usecases.internal_loan.dto.UpdateInternalLoanInput
import usecases.invoices.dto.CompleteInvoiceInput
import usecases.invoices.dto.CreateInvoiceInput
import usecases.invoices.dto.DeleteInvoiceInput
import usecases.invoices.dto.GetInvoiceOutput
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.UUID

@Configuration
class InternalLoanConfig {

    @Bean
    fun createInternalLoan(
        internalRepo: IRepository<InternalLoan>,
        accountRepo: IRepository<Account>,
        createInvoice: IInnerUseCase<CreateInvoiceInput, CreatedOutput>,
        getInvoice: IUseCase<UUID, GetInvoiceOutput>,
        invoiceRepo: IRepository<Invoice>,
        scheduleInvoiceRepo: IRepository<ScheduleInvoice>,
        unitOfWork: IUnitOfWork
    ): IUseCase<CreateInternalLoanInput, CreatedOutput> {
        return CreateInternalLoan(
            internalRepo,
            accountRepo,
            createInvoice,
            invoiceRepo,
            scheduleInvoiceRepo,
            getInvoice,

            unitOfWork
        )
    }

    @Bean
    fun updateInternalLoan(
        internalRepo: IRepository<InternalLoan>,
        accountRepo: IRepository<Account>,
    ): IUseCase<UpdateInternalLoanInput, Unit> {
        return UpdateInternalLoan(
            internalRepo,
            accountRepo
        )
    }

    @Bean
    fun deleteInternalLoan(
        internalLoanRepo: IRepository<InternalLoan>,
        deleteInvoice: IInnerUseCase<DeleteInvoiceInput, Unit>,
        unitOfWork: IUnitOfWork
    ): IUseCase<UUID, Unit> {
        return DeleteInternalLoan(
            internalLoanRepo,
            deleteInvoice,
            unitOfWork
        )
    }

    @Bean
    fun getInternalLoan(
        internalRepo: IRepository<InternalLoan>,
        getInvoice: IUseCase<UUID, GetInvoiceOutput>
    ): IUseCase<UUID, GetInternalLoanOutput> {
        return GetInternalLoan(
            internalRepo,
            getInvoice = getInvoice
        )
    }

    @Bean
    fun getAllInternalLoan(
        internalRepo: IRepository<InternalLoan>,
        getInvoice: IUseCase<UUID, GetInvoiceOutput>
    ): IUseCase<QueryFilter, ListOutput<GetInternalLoanOutput>> {
        return GetAllInternalLoan(
            internalRepo,
            getInvoice = getInvoice
        )
    }

    @Bean
    fun getAddRefundInternalLoan(
        internalRepo: IRepository<InternalLoan>,
        getInvoice: IUseCase<UUID, GetInvoiceOutput>,
        createInvoice: IInnerUseCase<CreateInvoiceInput, CreatedOutput>,
        unitOfWork: IUnitOfWork
    ): IUseCase<AddRefundInternalLoanInput, Unit> {
        return AddRefundInternalLoan(
            internalLoanRepo = internalRepo,
            getInvoice = getInvoice,
            createInvoice = createInvoice,
            unitOfWork = unitOfWork
        )
    }

    @Bean
    fun getRemoveRefundInternalLoan(
        internalRepo: IRepository<InternalLoan>,
        getInvoice: IUseCase<UUID, GetInvoiceOutput>,
        deleteInvoice: IInnerUseCase<DeleteInvoiceInput, Unit>,
        unitOfWork: IUnitOfWork
    ): IUseCase<RemoveRefundInternalLoanInput, Unit> {
        return RemoveRefundInternalLoan(
            internalLoanRepo = internalRepo,
            getInvoice = getInvoice,
            deleteInvoice = deleteInvoice,
            unitOfWork = unitOfWork
        )
    }


    @Bean
    fun autoCompleteInternalLoan(
        internalLoanRepo : IRepository<InternalLoan>,
        completeInvoice: IUseCase<CompleteInvoiceInput, Unit>,
        getInvoice: IUseCase<UUID, GetInvoiceOutput>,
        eventRegister: IEventRegister
    ): ISuspendableUseCase<Unit, BackgroundTaskOut> {
        return AutoCompleteInternalLoan(
            internalLoanRepo = internalLoanRepo,
            getInvoice = getInvoice,
            completeInvoice = completeInvoice,
            eventRegister = eventRegister
        )
    }
}