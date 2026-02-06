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
import dev.auguste.agni_api.core.usecases.invoices.dto.TransfertInvoiceInput

class TransfertInvoice(
    val invoiceRepo: IRepository<Invoice>,
    val accountRepo: IRepository<Account>,
    val transactionRepo: IRepository<Transaction>,
    val unitOfWork: IUnitOfWork
): IUseCase<TransfertInvoiceInput, Unit> {
    override fun execAsync(input: TransfertInvoiceInput) {
        try {
            unitOfWork.start()

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

            unitOfWork.commit()
        } catch (error: Throwable) {
            unitOfWork.rollback()
            throw error
        }
    }
}