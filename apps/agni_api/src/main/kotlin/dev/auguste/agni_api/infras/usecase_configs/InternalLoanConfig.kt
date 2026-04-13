package dev.auguste.agni_api.infras.usecase_configs

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.adapters.repositories.IUnitOfWork
import dev.auguste.agni_api.core.entities.Account
import dev.auguste.agni_api.core.entities.InternalLoan
import dev.auguste.agni_api.core.entities.Invoice
import dev.auguste.agni_api.core.entities.ScheduleInvoice
import dev.auguste.agni_api.core.usecases.CreatedOutput
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.interfaces.IInnerUseCase
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.internal_loan.CreateInternalLoan
import dev.auguste.agni_api.core.usecases.internal_loan.DeleteInternalLoan
import dev.auguste.agni_api.core.usecases.internal_loan.GetAllInternalLoan
import dev.auguste.agni_api.core.usecases.internal_loan.GetInternalLoan
import dev.auguste.agni_api.core.usecases.internal_loan.UpdateInternalLoan
import dev.auguste.agni_api.core.usecases.internal_loan.dto.CreateInternalLoanInput
import dev.auguste.agni_api.core.usecases.internal_loan.dto.GetInternalLoanOutput
import dev.auguste.agni_api.core.usecases.internal_loan.dto.UpdateInternalLoanInput
import dev.auguste.agni_api.core.usecases.invoices.dto.CreateInvoiceInput
import dev.auguste.agni_api.core.usecases.invoices.dto.DeleteInvoiceInput
import dev.auguste.agni_api.core.usecases.invoices.dto.GetInvoiceOutput
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
    ): IUseCase<UUID, GetInternalLoanOutput> {
        return GetInternalLoan(
            internalRepo
        )
    }

    @Bean
    fun getAllInternalLoan(
        internalRepo: IRepository<InternalLoan>,
    ): IUseCase<QueryFilter, ListOutput<GetInternalLoanOutput>> {
        return GetAllInternalLoan(
            internalRepo
        )
    }
}