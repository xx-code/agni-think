package dev.auguste.agni_api.infras.usecase_configs

import dev.auguste.agni_api.core.adapters.IEmbeddingService
import dev.auguste.agni_api.core.adapters.events.IEventRegister
import dev.auguste.agni_api.core.adapters.readers.IInvoicetransactionCountReader
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.adapters.repositories.IUnitOfWork
import dev.auguste.agni_api.core.entities.Account
import dev.auguste.agni_api.core.entities.Budget
import dev.auguste.agni_api.core.entities.Category
import dev.auguste.agni_api.core.entities.Deduction
import dev.auguste.agni_api.core.entities.Invoice
import dev.auguste.agni_api.core.entities.Tag
import dev.auguste.agni_api.core.entities.Transaction
import dev.auguste.agni_api.core.facades.InvoiceDependencies
import dev.auguste.agni_api.core.usecases.BackgroundTaskOut
import dev.auguste.agni_api.core.usecases.CreatedOutput
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.interfaces.IInnerUseCase
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.invoices.CompleteInvoice
import dev.auguste.agni_api.core.usecases.invoices.CreateFreezeInvoice
import dev.auguste.agni_api.core.usecases.invoices.CreateInvoice
import dev.auguste.agni_api.core.usecases.invoices.CreateInvoiceEmbedding
import dev.auguste.agni_api.core.usecases.invoices.DeleteInvoice
import dev.auguste.agni_api.core.usecases.invoices.DeleteInvoiceEmbedding
import dev.auguste.agni_api.core.usecases.invoices.GetAllInvoices
import dev.auguste.agni_api.core.usecases.invoices.GetBalance
import dev.auguste.agni_api.core.usecases.invoices.GetBalancesByPeriod
import dev.auguste.agni_api.core.usecases.invoices.GetInvoice
import dev.auguste.agni_api.core.usecases.invoices.RemoveFreezeInvoice
import dev.auguste.agni_api.core.usecases.invoices.TransferInvoice
import dev.auguste.agni_api.core.usecases.invoices.UpdateInvoice
import dev.auguste.agni_api.core.usecases.invoices.dto.CompleteInvoiceInput
import dev.auguste.agni_api.core.usecases.invoices.dto.CreateFreezeInvoiceInput
import dev.auguste.agni_api.core.usecases.invoices.dto.CreateInvoiceInput
import dev.auguste.agni_api.core.usecases.invoices.dto.DeleteInvoiceInput
import dev.auguste.agni_api.core.usecases.invoices.dto.GetAllInvoiceInput
import dev.auguste.agni_api.core.usecases.invoices.dto.GetBalanceInput
import dev.auguste.agni_api.core.usecases.invoices.dto.GetBalanceOutput
import dev.auguste.agni_api.core.usecases.invoices.dto.GetBalancesByPeriodInput
import dev.auguste.agni_api.core.usecases.invoices.dto.GetInvoiceOutput
import dev.auguste.agni_api.core.usecases.invoices.dto.TransferInvoiceInput
import dev.auguste.agni_api.core.usecases.invoices.dto.UpdateInvoiceInput
import dev.auguste.agni_api.core.usecases.invoices.transactions.GetInvoiceTransactions
import dev.auguste.agni_api.core.usecases.invoices.transactions.dto.GetInvoiceTransactionsInput
import dev.auguste.agni_api.core.usecases.invoices.transactions.dto.GetInvoiceTransactionsOutput
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.UUID

@Configuration
class InvoiceConfig {

    @Bean
    fun getInvoiceTransactions(
        invoiceRepo: IRepository<Invoice>,
        deductionRepo: IRepository<Deduction>,
        transactionRepo: IRepository<Transaction>
    ): IUseCase<GetInvoiceTransactionsInput, List<GetInvoiceTransactionsOutput>> {
        return GetInvoiceTransactions(
            invoiceRepo = invoiceRepo,
            deductionRepo = deductionRepo,
            transactionRepo = transactionRepo
        )
    }

    @Bean
    fun completeInvoice(
        invoiceRepo: IRepository<Invoice>,
        accountRepo: IRepository<Account>,
        getInvoiceTransactions: IUseCase<GetInvoiceTransactionsInput, List<GetInvoiceTransactionsOutput>>,
        unitOfWork: IUnitOfWork,
        ): IUseCase<CompleteInvoiceInput, Unit> {
        return CompleteInvoice(
            invoiceRepo = invoiceRepo,
            getInvoiceTransactions = getInvoiceTransactions,
            accountRepo = accountRepo,
            unitOfWork = unitOfWork
        )
    }

    @Bean
    fun createFreezeInvoice(
        createInvoice: IUseCase<CreateInvoiceInput, CreatedOutput>
    ): IUseCase<CreateFreezeInvoiceInput, CreatedOutput> {
        return CreateFreezeInvoice(
            createInvoice = createInvoice,
        )
    }

    @Bean
    fun createInvoice(
        invoiceRepo: IRepository<Invoice>,
        invoiceDependencies: InvoiceDependencies,
        unitOfWork: IUnitOfWork,
        eventRegister: IEventRegister
    ): IInnerUseCase<CreateInvoiceInput, CreatedOutput> {
        return CreateInvoice(
            invoiceRepo = invoiceRepo,
            invoiceDependencies = invoiceDependencies,
            unitOfWork = unitOfWork,
            eventRegister = eventRegister
        )
    }

    @Bean
    fun deleteInvoice(
        invoiceRepo: IRepository<Invoice>,
        transactionRepo: IRepository<Transaction>,
        accountRepo: IRepository<Account>,
        getInvoiceTransactions: IUseCase<GetInvoiceTransactionsInput, List<GetInvoiceTransactionsOutput>>,
        unitOfWork: IUnitOfWork,
        eventRegister: IEventRegister
    ): IInnerUseCase<DeleteInvoiceInput, Unit> {
        return DeleteInvoice(
            invoiceRepo = invoiceRepo,
            transactionRepo = transactionRepo,
            accountRepo = accountRepo,
            getInvoiceTransactions = getInvoiceTransactions,
            unitOfWork = unitOfWork,
            eventRegister = eventRegister
        )
    }

    @Bean
    fun getAllInvoice(
        invoiceRepo: IRepository<Invoice>,
        deductionRepo: IRepository<Deduction>,
        invoiceTransactionCountReader: IInvoicetransactionCountReader,
        getInvoiceTransactions: IUseCase<GetInvoiceTransactionsInput, List<GetInvoiceTransactionsOutput>>,
    ): IUseCase<GetAllInvoiceInput, ListOutput<GetInvoiceOutput>> {
        return GetAllInvoices(
            invoiceRepo = invoiceRepo,
            deductionRepo = deductionRepo,
            invoiceTransactionCountReader = invoiceTransactionCountReader,
            getInvoiceTransactions = getInvoiceTransactions
        )
    }

    @Bean
    fun getInvoice(
        invoiceRepo: IRepository<Invoice>,
        getInvoiceTransactions: IUseCase<GetInvoiceTransactionsInput, List<GetInvoiceTransactionsOutput>>,
    ): IUseCase<UUID, GetInvoiceOutput> {
        return GetInvoice(
            invoiceRepo = invoiceRepo,
            getInvoiceTransactions = getInvoiceTransactions
        )
    }

    @Bean
    fun getBalance(
        invoiceRepo: IRepository<Invoice>,
        getInvoiceTransactions: IUseCase<GetInvoiceTransactionsInput, List<GetInvoiceTransactionsOutput>>,
    ): IUseCase<GetBalanceInput, GetBalanceOutput> {
        return GetBalance(
            invoiceRepo = invoiceRepo,
            getInvoiceTransactions = getInvoiceTransactions
        )
    }

    @Bean
    fun getBalanceByPeriod(
        getBalance: IUseCase<GetBalanceInput, GetBalanceOutput>,
    ): IUseCase<GetBalancesByPeriodInput, List<GetBalanceOutput>> {
        return GetBalancesByPeriod(
            getBalance = getBalance
        )
    }

    @Bean
    fun removeFreezeInvoice(
        invoiceRepo: IRepository<Invoice>,
        accountRepo: IRepository<Account>,
        deleteInvoice: IInnerUseCase<DeleteInvoiceInput, Unit>,
        eventRegister: IEventRegister
    ): IUseCase<Unit, BackgroundTaskOut> {
        return RemoveFreezeInvoice(
            invoiceRepo = invoiceRepo,
            accountRepo = accountRepo,
            deleteInvoice = deleteInvoice,
            eventRegister = eventRegister
        )
    }

    @Bean
    fun transferInvoice(
        invoiceRepo: IRepository<Invoice>,
        accountRepo: IRepository<Account>,
        transactionRepo: IRepository<Transaction>,
        unitOfWork: IUnitOfWork
    ): IUseCase<TransferInvoiceInput, Unit> {
        return TransferInvoice(
            invoiceRepo = invoiceRepo,
            accountRepo = accountRepo,
            transactionRepo = transactionRepo,
            unitOfWork = unitOfWork
        )
    }

    @Bean
    fun updateInvoice(
        invoiceRepo: IRepository<Invoice>,
        invoiceDependencies: InvoiceDependencies,
        createInvoice: IInnerUseCase<CreateInvoiceInput, CreatedOutput>,
        deleteInvoice: IInnerUseCase<DeleteInvoiceInput, Unit>,
        getInvoiceTransactions: IUseCase<GetInvoiceTransactionsInput, List<GetInvoiceTransactionsOutput>>,
        unitOfWork: IUnitOfWork
    ): IUseCase<UpdateInvoiceInput, Unit> {
        return UpdateInvoice(
            invoiceRepo = invoiceRepo,
            invoiceDependencies = invoiceDependencies,
            createInvoice = createInvoice,
            deleteInvoice = deleteInvoice,
            getInvoiceTransactions = getInvoiceTransactions,
            unitOfWork = unitOfWork
        )
    }

    @Bean
    fun createEmbeddingInvoice(
        eventRegister: IEventRegister,
        categoryRepo: IRepository<Category>,
        budgetRepo: IRepository<Budget>,
        tagRepo: IRepository<Tag>,
        getInvoice: IUseCase<UUID, GetInvoiceOutput>,
        embeddingService: IEmbeddingService
    ) : IUseCase<UUID, BackgroundTaskOut> {
        return CreateInvoiceEmbedding(
            eventRegister = eventRegister,
            categoryRepo = categoryRepo,
            budgetRepo = budgetRepo,
            tagsRepo = tagRepo,
            getInvoice = getInvoice,
            embeddingService = embeddingService
        )
    }

    @Bean
    fun deleteEmbeddingInvoice(
        eventRegister: IEventRegister,
        embeddingService: IEmbeddingService
    ) : IUseCase<UUID, BackgroundTaskOut> {
        return DeleteInvoiceEmbedding(
            eventRegister = eventRegister,
            embeddingService = embeddingService
        )
    }

    @Bean
    fun facadeInvoiceDependencies(
        transactionRepo: IRepository<Transaction>,
        accountRepo: IRepository<Account>,
        deductionRepo: IRepository<Deduction>,
        tagRepo: IRepository<Tag>,
        categoryRepo: IRepository<Category>,
        budgetRepo: IRepository<Budget>
    ): InvoiceDependencies {
        return InvoiceDependencies(
            transactionRepo = transactionRepo,
            categoryRepo = categoryRepo,
            budgetRepo = budgetRepo,
            tagRepo = tagRepo,
            accountRepo = accountRepo,
            deductionRepo = deductionRepo,
        )
    }
}