package dev.auguste.agni_api.core.usecases.invoices

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.adapters.repositories.IUnitOfWork
import dev.auguste.agni_api.core.entities.Account
import dev.auguste.agni_api.core.entities.Invoice
import dev.auguste.agni_api.core.entities.enums.InvoiceMouvementType
import dev.auguste.agni_api.core.entities.enums.InvoiceStatusType
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.invoices.transactions.dto.GetInvoiceTransactionsInput
import dev.auguste.agni_api.core.usecases.invoices.transactions.dto.GetInvoiceTransactionsOutput
import java.util.UUID

class CompleteInvoice(
    val invoiceRepo: IRepository<Invoice>,
    val getInvoiceTransactions: IUseCase<GetInvoiceTransactionsInput, List<GetInvoiceTransactionsOutput>>,
    val accountRepo: IRepository<Account>,
    val unitOfWork: IUnitOfWork
): IUseCase<UUID, Unit> {
    override fun execAsync(input: UUID) {
        try {
            unitOfWork.start()

            val invoice = invoiceRepo.get(input) ?: throw Exception("Invoice with id $input not found")
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

            unitOfWork.commit()
        } catch (error: Throwable) {
            unitOfWork.rollback()
            throw error
        }
    }
}