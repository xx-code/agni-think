package usecases.invoices

import adapters.dto.AccountSnapshotBalanceInput
import usecases.interfaces.IUseCase

import adapters.dto.QueryFilter
import adapters.events.EventType
import adapters.events.IEventRegister
import adapters.events.contents.CreateEmbeddingInvoiceEventContent
import adapters.repositories.IAccountBalanceSnapshotRepository
import adapters.repositories.IRepository
import adapters.repositories.IUnitOfWork
import adapters.repositories.QueryExtendBuilder
import adapters.repositories.QueryComparator
import domain.entities.Account
import domain.exceptions.NotFoundException
import domain.entities.InternalLoan
import domain.entities.Invoice
import usecases.invoices.dto.CompleteInvoiceInput
import usecases.invoices.transactions.dto.GetInvoiceTransactionsInput
import usecases.invoices.transactions.dto.GetInvoiceTransactionsOutput
import domain.enums.InvoiceMovementType
import domain.enums.InvoiceStatusType
import usecases.UseCase
import java.time.LocalDateTime

class CompleteInvoice(
    private val invoiceRepo: IRepository<Invoice>,
    private val getInvoiceTransactions: IUseCase<GetInvoiceTransactionsInput, List<GetInvoiceTransactionsOutput>>,
    private val accountRepo: IRepository<Account>,
    private val internalLoanRepo: IRepository<InternalLoan>,
    private val snapshotAccountBalanceRepo: IAccountBalanceSnapshotRepository,
    unitOfWork: IUnitOfWork,
    private val eventRegister: IEventRegister
): UseCase<CompleteInvoiceInput, Unit>(unitOfWork) {
    override suspend fun process(input: CompleteInvoiceInput) {
        val invoice = invoiceRepo.get(input.invoiceId) ?: throw NotFoundException.SingleEntity(input.invoiceId, "invoice")
        val account = accountRepo.get(invoice.accountId) ?: throw NotFoundException.SingleEntity(invoice.accountId, "account")

        val transactions = getInvoiceTransactions.processDirect(GetInvoiceTransactionsInput(
            invoiceIds = setOf(invoice.id),
            categoryIds = null,
            tagIds = null,
            budgetIds = null,
            minAmount = null,
            maxAmount = null
        ))

        val balance = transactions.first().total

        invoice.status = InvoiceStatusType.COMPLETED
        if (invoice.movement == InvoiceMovementType.CREDIT)
            account.balance += balance
        else account.balance -= balance

        invoiceRepo.update(invoice)
        accountRepo.update(account)
        snapshotAccountBalanceRepo.makeSnapshot(
            AccountSnapshotBalanceInput(account.id, account.balance, LocalDateTime.now())
        )


        // TODO: To remove feature
//        val conditionInvoice = QueryExtendBuilder<InternalLoan>()
//            .addCondition("invoiceId", QueryComparator.Equal, input.invoiceId)
//        val internalLoans = internalLoanRepo.getAll(QueryFilter.queryAll(), conditionInvoice)
//        if (internalLoans.items.isNotEmpty())
//            internalLoanRepo.delete(internalLoans.items.first().invoiceId)

        eventRegister.notify(EventType.CREATE_INVOICE, CreateEmbeddingInvoiceEventContent(invoice))
    }
}