package usecases.invoices

import adapters.dto.AccountSnapshotBalanceInput
import usecases.interfaces.IUseCase

import domain.TRANSFERT_CATEGORY_ID
import adapters.dto.QueryFilter
import adapters.events.EventType
import adapters.events.IEventRegister
import adapters.events.contents.DeleteEmbeddingInvoiceEventContent
import adapters.repositories.IAccountBalanceSnapshotRepository
import adapters.repositories.IRepository
import adapters.repositories.IUnitOfWork
import adapters.repositories.QueryExtendBuilder
import adapters.repositories.QueryComparator
import domain.entities.Account
import domain.exceptions.NotFoundException
import domain.exceptions.ValidationException
import domain.entities.InternalLoan
import domain.entities.Invoice
import domain.entities.Transaction
import usecases.invoices.dto.DeleteInvoiceInput
import usecases.invoices.transactions.dto.GetInvoiceTransactionsInput
import usecases.invoices.transactions.dto.GetInvoiceTransactionsOutput
import domain.enums.InvoiceMovementType
import domain.enums.InvoiceStatusType
import usecases.UseCase
import java.time.LocalDateTime

class DeleteInvoice(
    private val invoiceRepo: IRepository<Invoice>,
    private val transactionRepo: IRepository<Transaction>,
    private val accountRepo: IRepository<Account>,
    private val internalLoanRepo: IRepository<InternalLoan>,
    private val getInvoiceTransactions: IUseCase<GetInvoiceTransactionsInput, List<GetInvoiceTransactionsOutput>>,
    private val snapshotAccountBalanceRepo: IAccountBalanceSnapshotRepository,
    unitOfWork: IUnitOfWork,
    private val eventRegister: IEventRegister
): UseCase<DeleteInvoiceInput, Unit>(unitOfWork) {

    override suspend fun process(input: DeleteInvoiceInput): Unit {
        val invoice = invoiceRepo.get(input.invoiceId) ?: throw NotFoundException.SingleEntity(input.invoiceId, "invoice")
        val account = accountRepo.get(invoice.accountId) ?: throw NotFoundException.SingleEntity(invoice.accountId, "account")

        if (input.checkInternalLoan) {
            // TODO: To remove
            val conditionInvoice = QueryExtendBuilder<InternalLoan>()
                .addCondition("invoiceId", QueryComparator.Equal, input.invoiceId)
            val internalLoans = internalLoanRepo.getAll(QueryFilter(queryAll = true), conditionInvoice)
            if (internalLoans.items.isNotEmpty()) {
                throw ValidationException.InternalLoanLinkCantBeDelete()
            }

            val internalLoanRefunds = internalLoanRepo.getAll(QueryFilter(queryAll = true), conditionInvoice)
            if (internalLoanRefunds.items.isNotEmpty()) {
                throw ValidationException.InternalLoanLinkCantBeDelete()
            }
        }

        val invoiceTransactions = getInvoiceTransactions.processDirect(GetInvoiceTransactionsInput(
            invoiceIds = setOf(invoice.id),
            categoryIds = null,
            tagIds = null,
            budgetIds = null,
            minAmount = null,
            maxAmount = null,
        ))

        if (invoiceTransactions.first().transactions.first().category.id == TRANSFERT_CATEGORY_ID &&
            input.checkTransfer)
            throw ValidationException.CanOnlyCancelTransfer()

        val transactionIds = invoiceTransactions.flatMap { it.transactions }.map { it.id }.toSet()
        transactionRepo.deleteManyByIds(transactionIds)

        invoiceRepo.delete(input.invoiceId)

        if (invoice.status == InvoiceStatusType.COMPLETED) {
            if (invoice.movement == InvoiceMovementType.CREDIT)
                account.balance -= invoiceTransactions.first().total
            else
                account.balance += invoiceTransactions.first().total

            accountRepo.update(account)

            snapshotAccountBalanceRepo.makeSnapshot(
                AccountSnapshotBalanceInput(account.id, account.balance, LocalDateTime.now())
            )
        }

        if (invoice.status == InvoiceStatusType.COMPLETED)
            eventRegister.notify(EventType.DELETE_INVOICE, DeleteEmbeddingInvoiceEventContent(input.invoiceId))

    }
}