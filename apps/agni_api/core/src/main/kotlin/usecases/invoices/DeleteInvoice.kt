package usecases.invoices

import domain.TRANSFERT_CATEGORY_ID
import adapters.dto.QueryFilter
import adapters.events.EventType
import adapters.events.IEventRegister
import adapters.events.contents.DeleteEmbeddingInvoiceEventContent
import adapters.repositories.IRepository
import adapters.repositories.IUnitOfWork
import adapters.repositories.query_extend.QueryInternalLoanExtend
import domain.entities.Account
import domain.exceptions.NotFoundException
import domain.exceptions.ValidationException
import domain.entities.InternalLoan
import domain.entities.Invoice
import domain.entities.Transaction
import usecases.interfaces.IInnerUseCase
import usecases.interfaces.IUseCase
import usecases.invoices.dto.DeleteInvoiceInput
import usecases.invoices.transactions.dto.GetInvoiceTransactionsInput
import usecases.invoices.transactions.dto.GetInvoiceTransactionsOutput

class DeleteInvoice(
    private val invoiceRepo: IRepository<Invoice>,
    private val transactionRepo: IRepository<Transaction>,
    private val accountRepo: IRepository<Account>,
    private val getInvoiceTransactions: IUseCase<GetInvoiceTransactionsInput, List<GetInvoiceTransactionsOutput>>,
    private val internalLoanRepo: IRepository<InternalLoan>,
    private val unitOfWork: IUnitOfWork,
    private val eventRegister: IEventRegister
): IInnerUseCase<DeleteInvoiceInput, Unit> {

    override fun execAsync(input: DeleteInvoiceInput): Unit {
        unitOfWork.execute {
            this.execInnerAsync(input)
        }
    }

    override fun execInnerAsync(input: DeleteInvoiceInput): Unit {
        val invoice = invoiceRepo.get(input.invoiceId) ?: throw NotFoundException.SingleEntity(input.invoiceId, "invoice")
        val account = accountRepo.get(invoice.accountId) ?: throw NotFoundException.SingleEntity(invoice.accountId, "account")

        if (input.checkInternalLoan) {
            val internalLoans = internalLoanRepo.getAll(QueryFilter(queryAll = true), QueryInternalLoanExtend(invoiceId = input.invoiceId))
            if (internalLoans.items.isNotEmpty()) {
                throw ValidationException.InternalLoanLinkCantBeDelete()
            }

            val internalLoanRefunds = internalLoanRepo.getAll(QueryFilter(queryAll = true), QueryInternalLoanExtend(refundFreezeId = input.invoiceId))
            if (internalLoanRefunds.items.isNotEmpty()) {
                throw ValidationException.InternalLoanLinkCantBeDelete()
            }
        }

        val invoiceTransactions = getInvoiceTransactions.execAsync(GetInvoiceTransactionsInput(
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

        if (invoice.statusType == _root_ide_package_.domain.enums.InvoiceStatusType.COMPLETED) {
            if (invoice.movementType == _root_ide_package_.domain.enums.InvoiceMovementType.CREDIT)
                account.balance -= invoiceTransactions.first().total
            else
                account.balance += invoiceTransactions.first().total

            accountRepo.update(account)
        }

        if (invoice.statusType == _root_ide_package_.domain.enums.InvoiceStatusType.COMPLETED)
            eventRegister.notify(EventType.DELETE_INVOICE, DeleteEmbeddingInvoiceEventContent(input.invoiceId))

    }
}