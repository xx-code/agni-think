package dev.auguste.agni_api.core.usecases.invoices

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.adapters.repositories.IUnitOfWork
import dev.auguste.agni_api.core.entities.Account
import dev.auguste.agni_api.core.entities.Invoice
import dev.auguste.agni_api.core.entities.Transaction
import dev.auguste.agni_api.core.entities.enums.InvoiceMouvementType
import dev.auguste.agni_api.core.entities.enums.InvoiceStatusType
import dev.auguste.agni_api.core.usecases.interfaces.IInnerUseCase
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.invoices.transactions.dto.GetInvoiceTransactionsInput
import dev.auguste.agni_api.core.usecases.invoices.transactions.dto.GetInvoiceTransactionsOutput
import java.util.UUID

class DeleteInvoice(
    val invoiceRepo: IRepository<Invoice>,
    val transactionRepo: IRepository<Transaction>,
    val accountRepo: IRepository<Account>,
    val getInvoiceTransactions: IUseCase<GetInvoiceTransactionsInput, List<GetInvoiceTransactionsOutput>>,
    val unitOfWork: IUnitOfWork
): IInnerUseCase<UUID, Unit> {

    override fun execAsync(input: UUID): Unit {
        try {
            unitOfWork.start()
            this.execInnerAsync(input)
            unitOfWork.commit()
        } catch (error: Throwable) {
            unitOfWork.rollback()
            throw error
        }
    }

    override fun execInnerAsync(input: UUID): Unit {
        val invoice = invoiceRepo.get(input) ?: throw Exception("Invoice with id $input not found")
        val account = accountRepo.get(invoice.accountId) ?: throw Exception("Account with id ${invoice.accountId} not found")

        val invoiceTransactions = getInvoiceTransactions.execAsync(GetInvoiceTransactionsInput(
            invoiceIds = setOf(invoice.id),
            categoryIds = null,
            tagIds = null,
            budgetIds = null,
            minAmount = null,
            maxAmount = null,
        ))

        transactionRepo.deleteManyByIds(invoiceTransactions.flatMap { it.transactions }.map { it.id }.toSet())

        invoiceRepo.delete(input)

        if (invoice.statusType == InvoiceStatusType.COMPLETED) {
            if (invoice.mouvementType == InvoiceMouvementType.CREDIT)
                account.balance -= invoiceTransactions.first().total
            else
                account.balance += invoiceTransactions.first().total

            accountRepo.update(account)
        }
    }
}