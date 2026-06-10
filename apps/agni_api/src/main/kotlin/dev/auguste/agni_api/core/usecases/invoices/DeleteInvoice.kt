package dev.auguste.agni_api.core.usecases.invoices

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.events.EventType
import dev.auguste.agni_api.core.adapters.events.IEventRegister
import dev.auguste.agni_api.core.adapters.events.contents.DeleteEmbeddingInvoiceEventContent
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.adapters.repositories.IUnitOfWork
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryInternalLoanExtend
import dev.auguste.agni_api.core.entities.Account
import dev.auguste.agni_api.core.entities.DomainException
import dev.auguste.agni_api.core.entities.InternalLoan
import dev.auguste.agni_api.core.entities.Invoice
import dev.auguste.agni_api.core.entities.Transaction
import dev.auguste.agni_api.core.entities.enums.InvoiceMouvementType
import dev.auguste.agni_api.core.entities.enums.InvoiceStatusType
import dev.auguste.agni_api.core.usecases.interfaces.IInnerUseCase
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.invoices.dto.DeleteInvoiceInput
import dev.auguste.agni_api.core.usecases.invoices.transactions.dto.GetInvoiceTransactionsInput
import dev.auguste.agni_api.core.usecases.invoices.transactions.dto.GetInvoiceTransactionsOutput

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
        val invoice = invoiceRepo.get(input.invoiceId) ?: throw DomainException.NotFound.Invoice(input.invoiceId)
        val account = accountRepo.get(invoice.accountId) ?: throw DomainException.NotFound.Account(invoice.accountId)

        val invoiceTransactions = getInvoiceTransactions.execAsync(GetInvoiceTransactionsInput(
            invoiceIds = setOf(invoice.id),
            categoryIds = null,
            tagIds = null,
            budgetIds = null,
            minAmount = null,
            maxAmount = null,
        ))

        transactionRepo.deleteManyByIds(invoiceTransactions.flatMap { it.transactions }.map { it.id }.toSet())

        invoiceRepo.delete(input.invoiceId)

        if (invoice.statusType == InvoiceStatusType.COMPLETED) {
            if (invoice.mouvementType == InvoiceMouvementType.CREDIT)
                account.balance -= invoiceTransactions.first().total
            else
                account.balance += invoiceTransactions.first().total

            accountRepo.update(account)
        }

        if (invoice.statusType == InvoiceStatusType.COMPLETED)
            eventRegister.notify(EventType.DELETE_INVOICE, DeleteEmbeddingInvoiceEventContent(input.invoiceId))

        val internalLoans = internalLoanRepo.getAll(QueryFilter(queryAll = true), QueryInternalLoanExtend(invoiceId = input.invoiceId))
        if (internalLoans.items.isNotEmpty()) {
            internalLoanRepo.delete(internalLoans.items.first().id)
        }
    }
}