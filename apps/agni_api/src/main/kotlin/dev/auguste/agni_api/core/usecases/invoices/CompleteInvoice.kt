package dev.auguste.agni_api.core.usecases.invoices

import dev.auguste.agni_api.core.adapters.events.EventType
import dev.auguste.agni_api.core.adapters.events.IEventRegister
import dev.auguste.agni_api.core.adapters.events.contents.CreateEmbeddingInvoiceEventContent
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.adapters.repositories.IUnitOfWork
import dev.auguste.agni_api.core.entities.Account
import dev.auguste.agni_api.core.entities.Invoice
import dev.auguste.agni_api.core.entities.enums.InvoiceMouvementType
import dev.auguste.agni_api.core.entities.enums.InvoiceStatusType
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.invoices.dto.CompleteInvoiceInput
import dev.auguste.agni_api.core.usecases.invoices.transactions.dto.GetInvoiceTransactionsInput
import dev.auguste.agni_api.core.usecases.invoices.transactions.dto.GetInvoiceTransactionsOutput
import java.util.UUID

class CompleteInvoice(
    private val invoiceRepo: IRepository<Invoice>,
    private val getInvoiceTransactions: IUseCase<GetInvoiceTransactionsInput, List<GetInvoiceTransactionsOutput>>,
    private val accountRepo: IRepository<Account>,
    private val unitOfWork: IUnitOfWork,
    private val eventRegister: IEventRegister
): IUseCase<CompleteInvoiceInput, Unit> {
    override fun execAsync(input: CompleteInvoiceInput) {
        unitOfWork.execute {
            val invoice = invoiceRepo.get(input.invoiceId) ?: throw Exception("Invoice with id $input not found")
            val account = accountRepo.get(invoice.accountId) ?: throw Exception("Account with id $invoice not found")

            val transactions = getInvoiceTransactions.execAsync(GetInvoiceTransactionsInput(
                invoiceIds = setOf(invoice.id),
                categoryIds = null,
                tagIds = null,
                budgetIds = null,
                minAmount = null,
                maxAmount = null
            ))

            val balance = transactions.first().total

            invoice.statusType = InvoiceStatusType.COMPLETED
            if (invoice.mouvementType == InvoiceMouvementType.CREDIT)
                account.balance += balance
            else account.balance -= balance

            invoiceRepo.update(invoice)
            accountRepo.update(account)

            eventRegister.notify(EventType.CREATE_EMBEDDING_SERVICE, CreateEmbeddingInvoiceEventContent(invoice))
        }
    }
}