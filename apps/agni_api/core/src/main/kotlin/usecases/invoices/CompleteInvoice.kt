package usecases.invoices

import adapters.dto.AccountSnapshotBalanceInput
import usecases.interfaces.IUseCase

import adapters.IAuthenticateAdapter
import adapters.dto.QueryFilter
import adapters.events.EventType
import adapters.events.IEventRegister
import adapters.events.contents.CreateEmbeddingInvoiceEventContent
import adapters.repositories.IAccountBalanceSnapshotRepository
import adapters.repositories.IRepository
import adapters.repositories.IUnitOfWork
import adapters.repositories.QueryExtendBuilder
import adapters.repositories.QueryComparator
import domain.SAVING_CATEGORY_ID
import domain.entities.Account
import domain.exceptions.NotFoundException
import domain.entities.Invoice
import domain.enums.InvoiceModuleLinkerType
import usecases.invoices.dto.CompleteInvoiceInput
import usecases.invoices.transactions.dto.GetInvoiceTransactionsInput
import usecases.invoices.transactions.dto.GetInvoiceTransactionsOutput
import domain.enums.InvoiceMovementType
import domain.enums.InvoiceStatusType
import domain.exceptions.ValidationException
import usecases.UseCase
import usecases.funds.dto.IncreaseFundInput
import usecases.invoices.dto.DeleteInvoiceInput
import java.time.LocalDateTime
import kotlin.text.category

class CompleteInvoice(
    private val invoiceRepo: IRepository<Invoice>,
    private val getInvoiceTransactions: IUseCase<GetInvoiceTransactionsInput, List<GetInvoiceTransactionsOutput>>,
    private val accountRepo: IRepository<Account>,
    private val snapshotAccountBalanceRepo: IAccountBalanceSnapshotRepository,
    private val eventRegister: IEventRegister,
    private val increaseFund: IUseCase<IncreaseFundInput, Unit>,
    private val deleteInvoice: IUseCase<DeleteInvoiceInput, Unit>,
    unitOfWork: IUnitOfWork,
    auth: IAuthenticateAdapter
): UseCase<CompleteInvoiceInput, Unit>(unitOfWork = unitOfWork, auth = auth) {
    override suspend fun process(input: CompleteInvoiceInput) {
        val invoice = invoiceRepo.get(input.invoiceId) ?: throw NotFoundException.SingleEntity(input.invoiceId, "invoice")

        if (invoice.status != InvoiceStatusType.PENDING)
            throw ValidationException.CannotCompleteInvoiceAlreadyComplete()

        val transactions = getInvoiceTransactions.processDirect(GetInvoiceTransactionsInput(
            invoiceIds = setOf(invoice.id),
            categoryIds = null,
            tagIds = null,
            budgetIds = null,
            minAmount = null,
            maxAmount = null
        ))

        val invoiceTransaction = getInvoiceTransactions.processDirect(GetInvoiceTransactionsInput(
            invoiceIds = setOf(invoice.id),
            categoryIds = null,
            tagIds = null,
            budgetIds = null,
            minAmount = null,
            maxAmount = null
        )).firstOrNull()

        if (invoiceTransaction == null)
            throw ValidationException.TransactionsMustNotBeEmpty()


        if (invoiceTransaction.transactions.first().category.id == SAVING_CATEGORY_ID) {
            completeSavingInvoice(invoice, transactions.first().total)
        } else {
            completeStandardInvoice(invoice, transactions.first().total)
        }
    }

    suspend fun completeStandardInvoice(invoice: Invoice, invoiceBalance: Double) {
        val account = accountRepo.get(invoice.accountId) ?: throw NotFoundException.SingleEntity(invoice.accountId, "account")

        invoice.status = InvoiceStatusType.COMPLETED
        if (invoice.movement == InvoiceMovementType.CREDIT)
            account.balance += invoiceBalance
        else account.balance -= invoiceBalance

        invoiceRepo.update(invoice)
        accountRepo.update(account)
        snapshotAccountBalanceRepo.makeSnapshot(
            AccountSnapshotBalanceInput(account.userId, account.id, account.balance, LocalDateTime.now())
        )

        eventRegister.notify(EventType.CREATE_INVOICE, CreateEmbeddingInvoiceEventContent(invoice))
    }

    suspend fun completeSavingInvoice(invoice: Invoice, invoiceBalance: Double) {
        val invoice = invoiceRepo.get(invoice.id) ?: throw NotFoundException.SingleEntity(invoice.id, "invoice")

        val module = invoice.moduleLinkers.firstOrNull { it.module == InvoiceModuleLinkerType.FUND }

        if (module == null)
            throw ValidationException.CompleteInvoiceFundInvalidNotFund()

        increaseFund.processDirect(IncreaseFundInput(
            userId = invoice.userId,
            fundId = module.sourceId,
            accountId = invoice.accountId,
            amount =  invoiceBalance
        ))

        deleteInvoice.processDirect(DeleteInvoiceInput(
            invoiceId = invoice.id,
            checkSinkingFund = false
        ))
    }

}