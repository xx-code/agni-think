package dev.auguste.agni_api.core.usecases.invoices

import dev.auguste.agni_api.core.TRANSFERT_CATEGORY_ID
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.adapters.repositories.IUnitOfWork
import dev.auguste.agni_api.core.entities.Account
import dev.auguste.agni_api.core.entities.Invoice
import dev.auguste.agni_api.core.entities.Transaction
import dev.auguste.agni_api.core.entities.enums.InvoiceMouvementType
import dev.auguste.agni_api.core.entities.enums.InvoiceStatusType
import dev.auguste.agni_api.core.entities.enums.InvoiceType
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.invoices.dto.TransferInvoiceInput

class TransferInvoice(
    private val invoiceRepo: IRepository<Invoice>,
    private val accountRepo: IRepository<Account>,
    private val transactionRepo: IRepository<Transaction>,
    private val unitOfWork: IUnitOfWork
): IUseCase<TransferInvoiceInput, Unit> {
    override fun execAsync(input: TransferInvoiceInput) {
        unitOfWork.execute {
            val accountFrom = accountRepo.get(input.accountIdFrom) ?: throw Error("Account From not found")
            val accountTo = accountRepo.get(input.accountIdTo) ?: throw Error("Account To not found")

            if (input.amount < 0)
                throw Error("Amount must be non-negative")

            val invoiceFrom = Invoice(
                accountId = accountFrom.id,
                status = InvoiceStatusType.COMPLETED,
                date = input.date,
                type = InvoiceType.OTHER,
                mouvementType = InvoiceMouvementType.DEBIT,
            )

            val invoiceTo = Invoice(
                accountId = accountTo.id,
                status = InvoiceStatusType.COMPLETED,
                date = input.date,
                type = InvoiceType.OTHER,
                mouvementType = InvoiceMouvementType.CREDIT
            )

            invoiceRepo.create(invoiceFrom)
            invoiceRepo.create(invoiceTo)

            val transactionFrom = Transaction(
                invoiceId = invoiceFrom.id,
                amount = input.amount,
                categoryId = TRANSFERT_CATEGORY_ID,
                description = "Transfert du compte ${accountFrom.title} au compte ${accountTo.title}"
            )

            val transactionTo = Transaction(
                invoiceId = invoiceTo.id,
                amount = input.amount,
                categoryId = TRANSFERT_CATEGORY_ID,
                description = "Transfert depuis le compte ${accountFrom.title} au compte ${accountTo.title}"
            )

            transactionRepo.create(transactionFrom)
            transactionRepo.create(transactionTo)

            accountFrom.balance -= input.amount
            accountRepo.update(accountFrom)

            accountTo.balance += input.amount
            accountRepo.update(accountTo)
        }
    }
}