package usecase_configs

import adapters.IAuthenticateAdapter
import adapters.events.IEventRegister
import adapters.events.listeners.ICreateExternalTransactionListener
import adapters.events.listeners.ICreateInvoiceEventListener
import adapters.events.listeners.ICreateManyExternalTransactionListener
import adapters.events.listeners.IDeleteInvoiceEventListener
import adapters.readers.IInvoiceTransactionReader
import adapters.repositories.IAccountBalanceSnapshotRepository
import adapters.repositories.IRepository
import adapters.repositories.IUnitOfWork
import adapters.repositories.IVectorRepository
import usecases.invoices.DeleteInvoiceEmbedding
import domain.entities.Account
import domain.entities.Budget
import domain.entities.Category
import domain.entities.Deduction
import domain.entities.ExternalTransaction
import domain.entities.Fund
import domain.entities.Invoice
import domain.entities.Tag
import domain.entities.Transaction
import domain.entities.User
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
import usecases.funds.dto.IncreaseFundInput
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
        unitOfWork: IUnitOfWork,
        eventRegister: IEventRegister,
        increaseFund: IUseCase<IncreaseFundInput, Unit>,
        deleteInvoice: IUseCase<DeleteInvoiceInput, Unit>,
        snapshotBalanceAccountRepo: IAccountBalanceSnapshotRepository,
        auth: IAuthenticateAdapter,
        ): UseCase<CompleteInvoiceInput, Unit> {
        return CompleteInvoice(
            invoiceRepo = invoiceRepo,
            getInvoiceTransactions = getInvoiceTransactions,
            accountRepo = accountRepo,
            unitOfWork = unitOfWork,
            eventRegister = eventRegister,
            snapshotAccountBalanceRepo = snapshotBalanceAccountRepo,
            auth = auth,
            increaseFund = increaseFund,
            deleteInvoice = deleteInvoice,
        )
    }

    @Bean
    fun createFreezeInvoice(
        unitOfWork: IUnitOfWork,
        createInvoice: IUseCase<CreateInvoiceInput, CreatedOutput>,
    ): UseCase<CreateFreezeInvoiceInput, CreatedOutput> {
        return CreateFreezeInvoice(
            createInvoice = createInvoice,
            unitOfWork = unitOfWork,
        )
    }

    @Bean
    fun createInvoice(
        invoiceRepo: IRepository<Invoice>,
        invoiceDependencies: InvoiceDependencies,
        unitOfWork: IUnitOfWork,
        eventRegister: IEventRegister,
        snapshotBalanceAccountRepo: IAccountBalanceSnapshotRepository,
        userRepo: IRepository<User>,
    ): UseCase<CreateInvoiceInput, CreatedOutput> {
        return CreateInvoice(
            invoiceRepo = invoiceRepo,
            invoiceDependencies = invoiceDependencies,
            unitOfWork = unitOfWork,
            eventRegister = eventRegister,
            userRepo = userRepo,
            snapshotAccountBalanceRepo = snapshotBalanceAccountRepo,
        )
    }

    @Bean
    fun deleteInvoice(
        invoiceRepo: IRepository<Invoice>,
        transactionRepo: IRepository<Transaction>,
        accountRepo: IRepository<Account>,
        getInvoiceTransactions: IUseCase<GetInvoiceTransactionsInput, List<GetInvoiceTransactionsOutput>>,
        unitOfWork: IUnitOfWork,
        eventRegister: IEventRegister,
        fundRepo: IRepository<Fund>,
        updateFundScheduleInvoice: IUseCase<Fund, Unit>,
        snapshotBalanceAccountRepo: IAccountBalanceSnapshotRepository,
        auth: IAuthenticateAdapter,
    ): UseCase<DeleteInvoiceInput, Unit> {
        return DeleteInvoice(
            invoiceRepo = invoiceRepo,
            transactionRepo = transactionRepo,
            accountRepo = accountRepo,
            getInvoiceTransactions = getInvoiceTransactions,
            unitOfWork = unitOfWork,
            eventRegister = eventRegister,
            snapshotAccountBalanceRepo = snapshotBalanceAccountRepo,
            auth = auth,
            fundRepo = fundRepo,
            updateFundScheduleInvoice = updateFundScheduleInvoice
        )
    }

    @Bean
    fun getAllInvoice(
        invoiceRepo: IRepository<Invoice>,
        deductionRepo: IRepository<Deduction>,
        invoiceTransactionCountReader: IInvoiceTransactionReader,
        getInvoiceTransactions: IUseCase<GetInvoiceTransactionsInput, List<GetInvoiceTransactionsOutput>>,
        auth: IAuthenticateAdapter,
    ): UseCase<GetAllInvoiceInput, ListOutput<GetInvoiceOutput>> {
        return GetAllInvoices(
            invoiceRepo = invoiceRepo,
            deductionRepo = deductionRepo,
            invoiceTransactionReader = invoiceTransactionCountReader,
            getInvoiceTransactions = getInvoiceTransactions,
            auth = auth
        )
    }

    @Bean
    fun getInvoice(
        invoiceRepo: IRepository<Invoice>,
        getInvoiceTransactions: IUseCase<GetInvoiceTransactionsInput, List<GetInvoiceTransactionsOutput>>,
        auth: IAuthenticateAdapter,
    ): UseCase<UUID, GetInvoiceOutput> {
        return GetInvoice(
            invoiceRepo = invoiceRepo,
            auth = auth,
            getInvoiceTransactions = getInvoiceTransactions
        )
    }

    @Bean
    fun getManyInvoice(
        invoiceRepo: IRepository<Invoice>,
        getInvoiceTransactions: IUseCase<GetInvoiceTransactionsInput, List<GetInvoiceTransactionsOutput>>,
        auth: IAuthenticateAdapter,
    ): UseCase<Set<UUID>, List<GetInvoiceOutput>> {
        return GetManyInvoices(
            invoiceRepo = invoiceRepo,
            auth = auth,
            getInvoiceTransactions = getInvoiceTransactions
        )
    }

    @Bean
    fun getBalance(
        invoiceRepo: IRepository<Invoice>,
        getInvoiceTransactions: IUseCase<GetInvoiceTransactionsInput, List<GetInvoiceTransactionsOutput>>,
        userRepo: IRepository<User>,
    ): UseCase<GetBalanceInput, GetBalanceOutput> {
        return GetBalance(
            userRepo = userRepo,
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
        userRepo: IRepository<User>,
        unitOfWork: IUnitOfWork,
        snapshotRepository: IAccountBalanceSnapshotRepository,
    ): UseCase<TransferInvoiceInput, Unit> {
        return TransferInvoice(
            invoiceRepo = invoiceRepo,
            userRepo = userRepo,
            accountRepo = accountRepo,
            transactionRepo = transactionRepo,
            unitOfWork = unitOfWork,
            snapshotAccountBalanceRepo = snapshotRepository,
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
        unitOfWork: IUnitOfWork,
        auth: IAuthenticateAdapter,
    ): UseCase<UpdateInvoiceInput, Unit> {
        return UpdateInvoice(
            invoiceRepo = invoiceRepo,
            invoiceDependencies = invoiceDependencies,
            createInvoice = createInvoice,
            deleteInvoice = deleteInvoice,
            getInvoiceTransactions = getInvoiceTransactions,
            unitOfWork = unitOfWork,
            auth = auth
        )
    }

    @Bean
    fun createEmbeddingInvoice(
        eventRegister: IEventRegister,
        categoryRepo: IRepository<Category>,
        budgetRepo: IRepository<Budget>,
        tagRepo: IRepository<Tag>,
        getInvoice: IUseCase<UUID, GetInvoiceOutput>,
        vectorRepo: IVectorRepository,
        @Value("\${embedding.collection.invoice}") collectionName: String
    ) : ICreateInvoiceEventListener {
        return CreateInvoiceEmbedding(
            eventRegister = eventRegister,
            categoryRepo = categoryRepo,
            budgetRepo = budgetRepo,
            tagsRepo = tagRepo,
            getInvoice = getInvoice,
            vectorRepo = vectorRepo,
            collectionName
        )
    }

    @Bean
    fun deleteEmbeddingInvoice(
        eventRegister: IEventRegister,
        vectorRepo: IVectorRepository,
        @Value("\${embedding.collection.invoice}") collectionName: String
    ) : IDeleteInvoiceEventListener {
        return DeleteInvoiceEmbedding(
            eventRegister = eventRegister,
            vectorRepo = vectorRepo,
            collectionName
        )
    }

    @Bean
    fun addExternalTransactions(
        externalTransactionRepo: IRepository<ExternalTransaction>,
        eventRegister: IEventRegister,
        auth: IAuthenticateAdapter,
    ): UseCase<AddExternalTransactionInput, CreatedOutput> {
        return AddExternalTransaction(
            auth = auth,
            externalTransactionRepo = externalTransactionRepo,
            eventRegister = eventRegister,
        )
    }
    @Bean
    fun addManyExternalTransactions(
        externalTransactionRepo: IRepository<ExternalTransaction>,
        auth: IAuthenticateAdapter,
        eventRegister: IEventRegister,
    ): UseCase<List<AddExternalTransactionInput>, List<CreatedOutput>> {
        return AddManyExternalTransactions(
            externalTransRepo = externalTransactionRepo,
            auth = auth,
            eventRegister = eventRegister,
        )
    }

    @Bean
    fun getAllExternalTransactions(
        externalTransactionRepo: IRepository<ExternalTransaction>,
        auth: IAuthenticateAdapter,
    ): UseCase<GetAllExternalTransactionInput, ListOutput<GetExternalTransactionOutput>> {
        return GetAllExternalTransaction(
            externalTransactionRepo = externalTransactionRepo,
            auth = auth
        )
    }

    @Bean
    fun treatAnExternalTransactions(
        externalTransactionRepo: IRepository<ExternalTransaction>,
        eventRegister: IEventRegister,
        auth: IAuthenticateAdapter,
    ): UseCase<TreatAnExternalTransactionInput, Unit> {
        return TreatAnExternalTransaction(externalTransactionRepo, eventRegister, auth)
    }

    @Bean
    fun facadeInvoiceDependencies(
        transactionRepo: IRepository<Transaction>,
        accountRepo: IRepository<Account>,
        deductionRepo: IRepository<Deduction>,
        tagRepo: IRepository<Tag>,
        categoryRepo: IRepository<Category>,
        budgetRepo: IRepository<Budget>,
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

    @Bean
    fun createEmbeddingExternalTransactions(
        eventRegister: IEventRegister,
        externalTransRepo: IRepository<ExternalTransaction>,
        vectorRepo: IVectorRepository,
        @Value("\${embedding.collection.external-transaction}") collectionName: String
    ) : ICreateExternalTransactionListener {
        return CreateExternalTransaction(
            externalTransRepo,
            vectorRepo,
            eventRegister,
            collectionName
        )
    }

    @Bean
    fun createManyEmbeddingExternalTransactions(
        eventRegister: IEventRegister,
        externalTransRepo: IRepository<ExternalTransaction>,
        vectorRepo: IVectorRepository,
        @Value("\${embedding.collection.external-transaction}") collectionName: String
    ) : ICreateManyExternalTransactionListener {
        return CreateManyExternalTransactionEmbedding(
            eventRegister = eventRegister,
            externalTransactionRepo = externalTransRepo,
            vectorRepo = vectorRepo,
            collectionName=collectionName
        )
    }
}