package usecase_configs

import adapters.IEmbeddingService
import adapters.events.IEventRegister
import adapters.events.listeners.ICreateExternalTransactionListener
import adapters.events.listeners.ICreateInvoiceEventListener
import adapters.events.listeners.ICreateManyExternalTransactionListener
import adapters.events.listeners.IDeleteInvoiceEventListener
import adapters.readers.IInvoiceTransactionReader
import adapters.repositories.IRepository
import adapters.repositories.IUnitOfWork
import usecases.invoices.DeleteInvoiceEmbedding
import domain.entities.Account
import domain.entities.Budget
import domain.entities.Category
import domain.entities.Deduction
import domain.entities.ExternalTransaction
import domain.entities.InternalLoan
import domain.entities.Invoice
import domain.entities.Tag
import domain.entities.Transaction
import facades.InvoiceDependencies
import usecases.dto.BackgroundTaskOut
import usecases.dto.CreatedOutput
import usecases.dto.ListOutput
import usecases.interfaces.IUseCase
import usecases.invoices.AddExternalTransaction
import usecases.invoices.AddManyExternalTransactions
import usecases.invoices.CancelTransfer
import usecases.invoices.CompleteInvoice
import usecases.invoices.CreateExternalTransaction
import usecases.invoices.CreateFreezeInvoice
import usecases.invoices.CreateInvoice
import usecases.invoices.CreateInvoiceEmbedding
import usecases.invoices.CreateManyExternalTransactionEmbedding
import usecases.invoices.DeleteInvoice
import usecases.invoices.GetAllExternalTransaction
import usecases.invoices.GetAllInvoices
import usecases.invoices.GetBalance
import usecases.invoices.GetBalancesByPeriod
import usecases.invoices.GetInvoice
import usecases.invoices.GetManyInvoices
import usecases.invoices.RemoveFreezeInvoice
import usecases.invoices.TransferInvoice
import usecases.invoices.TreatAnExternalTransaction
import usecases.invoices.UpdateInvoice
import usecases.invoices.dto.AddExternalTransactionInput
import usecases.invoices.dto.CompleteInvoiceInput
import usecases.invoices.dto.CreateFreezeInvoiceInput
import usecases.invoices.dto.CreateInvoiceInput
import usecases.invoices.dto.DeleteInvoiceInput
import usecases.invoices.dto.GetAllExternalTransactionInput
import usecases.invoices.dto.GetAllInvoiceInput
import usecases.invoices.dto.GetBalanceByPeriodOutput
import usecases.invoices.dto.GetBalanceInput
import usecases.invoices.dto.GetBalanceOutput
import usecases.invoices.dto.GetBalancesByPeriodInput
import usecases.invoices.dto.GetExternalTransactionOutput
import usecases.invoices.dto.GetInvoiceOutput
import usecases.invoices.dto.TransferInvoiceInput
import usecases.invoices.dto.TreatAnExternalTransactionInput
import usecases.invoices.dto.UpdateInvoiceInput
import usecases.invoices.transactions.GetInvoiceTransactions
import usecases.invoices.transactions.dto.GetInvoiceTransactionsInput
import usecases.invoices.transactions.dto.GetInvoiceTransactionsOutput
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import usecases.UseCase
import java.util.UUID

@Configuration
class InvoiceConfig {

    @Bean
    fun getInvoiceTransactions(
        invoiceRepo: IRepository<Invoice>,
        deductionRepo: IRepository<Deduction>,
        transactionRepo: IRepository<Transaction>,
        categoryRepo: IRepository<Category>,
        budgetRepo: IRepository<Budget>,
        tagRepo: IRepository<Tag>,
    ): UseCase<GetInvoiceTransactionsInput, List<GetInvoiceTransactionsOutput>> {
        return GetInvoiceTransactions(
            invoiceRepo = invoiceRepo,
            deductionRepo = deductionRepo,
            transactionRepo = transactionRepo,
            categoryRepo = categoryRepo,
            budgetRepo = budgetRepo,
            tagRepo = tagRepo
        )
    }

    @Bean
    fun completeInvoice(
        invoiceRepo: IRepository<Invoice>,
        accountRepo: IRepository<Account>,
        getInvoiceTransactions: IUseCase<GetInvoiceTransactionsInput, List<GetInvoiceTransactionsOutput>>,
        internalLoanRepo: IRepository<InternalLoan>,
        unitOfWork: IUnitOfWork,
        eventRegister: IEventRegister,
        ): UseCase<CompleteInvoiceInput, Unit> {
        return CompleteInvoice(
            invoiceRepo = invoiceRepo,
            getInvoiceTransactions = getInvoiceTransactions,
            accountRepo = accountRepo,
            unitOfWork = unitOfWork,
            internalLoanRepo = internalLoanRepo,
            eventRegister = eventRegister
        )
    }

    @Bean
    fun createFreezeInvoice(
        unitOfWork: IUnitOfWork,
        createInvoice: IUseCase<CreateInvoiceInput, CreatedOutput>
    ): UseCase<CreateFreezeInvoiceInput, CreatedOutput> {
        return CreateFreezeInvoice(
            createInvoice = createInvoice,
            unitOfWork = unitOfWork
        )
    }

    @Bean
    fun createInvoice(
        invoiceRepo: IRepository<Invoice>,
        invoiceDependencies: InvoiceDependencies,
        unitOfWork: IUnitOfWork,
        eventRegister: IEventRegister
    ): UseCase<CreateInvoiceInput, CreatedOutput> {
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
        internalLoanRepo: IRepository<InternalLoan>,
        unitOfWork: IUnitOfWork,
        eventRegister: IEventRegister
    ): UseCase<DeleteInvoiceInput, Unit> {
        return DeleteInvoice(
            invoiceRepo = invoiceRepo,
            transactionRepo = transactionRepo,
            accountRepo = accountRepo,
            getInvoiceTransactions = getInvoiceTransactions,
            unitOfWork = unitOfWork,
            internalLoanRepo = internalLoanRepo,
            eventRegister = eventRegister
        )
    }

    @Bean
    fun getAllInvoice(
        invoiceRepo: IRepository<Invoice>,
        deductionRepo: IRepository<Deduction>,
        invoiceTransactionCountReader: IInvoiceTransactionReader,
        getInvoiceTransactions: IUseCase<GetInvoiceTransactionsInput, List<GetInvoiceTransactionsOutput>>,
    ): UseCase<GetAllInvoiceInput, ListOutput<GetInvoiceOutput>> {
        return GetAllInvoices(
            invoiceRepo = invoiceRepo,
            deductionRepo = deductionRepo,
            invoiceTransactionReader = invoiceTransactionCountReader,
            getInvoiceTransactions = getInvoiceTransactions
        )
    }

    @Bean
    fun getInvoice(
        invoiceRepo: IRepository<Invoice>,
        getInvoiceTransactions: IUseCase<GetInvoiceTransactionsInput, List<GetInvoiceTransactionsOutput>>,
    ): UseCase<UUID, GetInvoiceOutput> {
        return GetInvoice(
            invoiceRepo = invoiceRepo,
            getInvoiceTransactions = getInvoiceTransactions
        )
    }

    @Bean
    fun getManyInvoice(
        invoiceRepo: IRepository<Invoice>,
        getInvoiceTransactions: IUseCase<GetInvoiceTransactionsInput, List<GetInvoiceTransactionsOutput>>,
    ): UseCase<Set<UUID>, List<GetInvoiceOutput>> {
        return GetManyInvoices(
            invoiceRepo = invoiceRepo,
            getInvoiceTransactions = getInvoiceTransactions
        )
    }

    @Bean
    fun getBalance(
        invoiceRepo: IRepository<Invoice>,
        getInvoiceTransactions: IUseCase<GetInvoiceTransactionsInput, List<GetInvoiceTransactionsOutput>>,
    ): UseCase<GetBalanceInput, GetBalanceOutput> {
        return GetBalance(
            invoiceRepo = invoiceRepo,
            getInvoiceTransactions = getInvoiceTransactions
        )
    }

    @Bean
    fun getBalanceByPeriod(
        getBalance: IUseCase<GetBalanceInput, GetBalanceOutput>,
    ): UseCase<GetBalancesByPeriodInput, List<GetBalanceByPeriodOutput>> {
        return GetBalancesByPeriod(
            getBalance = getBalance
        )
    }

    @Bean
    fun removeFreezeInvoice(
        invoiceRepo: IRepository<Invoice>,
        accountRepo: IRepository<Account>,
        deleteInvoice: IUseCase<DeleteInvoiceInput, Unit>,
        eventRegister: IEventRegister
    ): UseCase<Unit, BackgroundTaskOut> {
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
    ): UseCase<TransferInvoiceInput, Unit> {
        return TransferInvoice(
            invoiceRepo = invoiceRepo,
            accountRepo = accountRepo,
            transactionRepo = transactionRepo,
            unitOfWork = unitOfWork
        )
    }

    @Bean
    fun cancelTransferInvoice(
        invoiceRepo: IRepository<Invoice>,
        transactionRepo: IRepository<Transaction>,
        deleteInvoice: IUseCase<DeleteInvoiceInput, Unit>,
        unitOfWork: IUnitOfWork
    ): UseCase<UUID, Unit> {
        return CancelTransfer(
            invoiceRepo = invoiceRepo,
            transactionRepo = transactionRepo,
            deleteInvoice = deleteInvoice,
            unitOfWork = unitOfWork
        )
    }

    @Bean
    fun updateInvoice(
        invoiceRepo: IRepository<Invoice>,
        invoiceDependencies: InvoiceDependencies,
        createInvoice: IUseCase<CreateInvoiceInput, CreatedOutput>,
        deleteInvoice: IUseCase<DeleteInvoiceInput, Unit>,
        getInvoiceTransactions: IUseCase<GetInvoiceTransactionsInput, List<GetInvoiceTransactionsOutput>>,
        unitOfWork: IUnitOfWork
    ): UseCase<UpdateInvoiceInput, Unit> {
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
        embeddingService: IEmbeddingService,
        @Value("\${embedding.collection.invoice}") collectionName: String
    ) : ICreateInvoiceEventListener {
        return CreateInvoiceEmbedding(
            eventRegister = eventRegister,
            categoryRepo = categoryRepo,
            budgetRepo = budgetRepo,
            tagsRepo = tagRepo,
            getInvoice = getInvoice,
            embeddingService = embeddingService,
            collectionName
        )
    }

    @Bean
    fun deleteEmbeddingInvoice(
        eventRegister: IEventRegister,
        embeddingService: IEmbeddingService,
        @Value("\${embedding.collection.invoice}") collectionName: String
    ) : IDeleteInvoiceEventListener {
        return DeleteInvoiceEmbedding(
            eventRegister = eventRegister,
            embeddingService = embeddingService,
            collectionName
        )
    }

    @Bean
    fun addExternalTransactions(
        externalTransactionRepo: IRepository<ExternalTransaction>,
        eventRegister: IEventRegister,
    ): UseCase<AddExternalTransactionInput, CreatedOutput> {
        return AddExternalTransaction(
            externalTransactionRepo = externalTransactionRepo,
            eventRegister = eventRegister,
        )
    }
    @Bean
    fun addManyExternalTransactions(
        externalTransactionRepo: IRepository<ExternalTransaction>,
        eventRegister: IEventRegister,
    ): UseCase<List<AddExternalTransactionInput>, List<CreatedOutput>> {
        return AddManyExternalTransactions(
            externalTransRepo = externalTransactionRepo,
            eventRegister = eventRegister,
        )
    }

    @Bean
    fun getAllExternalTransactions(
        externalTransactionRepo: IRepository<ExternalTransaction>,
    ): UseCase<GetAllExternalTransactionInput, ListOutput<GetExternalTransactionOutput>> {
        return GetAllExternalTransaction(
            externalTransactionRepo = externalTransactionRepo
        )
    }

    @Bean
    fun treatAnExternalTransactions(
        externalTransactionRepo: IRepository<ExternalTransaction>,
        eventRegister: IEventRegister,
    ): UseCase<TreatAnExternalTransactionInput, Unit> {
        return TreatAnExternalTransaction(externalTransactionRepo, eventRegister)
    }

    @Bean
    fun facadeInvoiceDependencies(
        transactionRepo: IRepository<Transaction>,
        accountRepo: IRepository<Account>,
        deductionRepo: IRepository<Deduction>,
        tagRepo: IRepository<Tag>,
        categoryRepo: IRepository<Category>,
        budgetRepo: IRepository<Budget>,
        internalLoaRepository: IRepository<InternalLoan>,
    ): InvoiceDependencies {
        return InvoiceDependencies(
            transactionRepo = transactionRepo,
            categoryRepo = categoryRepo,
            budgetRepo = budgetRepo,
            tagRepo = tagRepo,
            accountRepo = accountRepo,
            deductionRepo = deductionRepo,
            internalLoanRepo =  internalLoaRepository
        )
    }

    @Bean
    fun createEmbeddingExternalTransactions(
        eventRegister: IEventRegister,
        externalTransRepo: IRepository<ExternalTransaction>,
        embeddingService: IEmbeddingService,
        @Value("\${embedding.collection.external-transaction}") collectionName: String
    ) : ICreateExternalTransactionListener {
        return CreateExternalTransaction(
            externalTransRepo,
            embeddingService,
            eventRegister,
            collectionName
        )
    }

    @Bean
    fun createManyEmbeddingExternalTransactions(
        eventRegister: IEventRegister,
        externalTransRepo: IRepository<ExternalTransaction>,
        embeddingService: IEmbeddingService,
        @Value("\${embedding.collection.external-transaction}") collectionName: String
    ) : ICreateManyExternalTransactionListener {
        return CreateManyExternalTransactionEmbedding(
            eventRegister = eventRegister,
            externalTransactionRepo = externalTransRepo,
            embeddingService = embeddingService,
            collectionName=collectionName
        )
    }
}