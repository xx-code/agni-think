package dev.auguste.agni_api.core.usecases.invoices

import dev.auguste.agni_api.core.TRANSFERT_CATEGORY_ID
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.adapters.repositories.IUnitOfWork
import dev.auguste.agni_api.core.entities.Account
import dev.auguste.agni_api.core.entities.DomainException
import dev.auguste.agni_api.core.entities.Invoice
import dev.auguste.agni_api.core.entities.Transaction
import dev.auguste.agni_api.core.entities.enums.InvoiceModuleLinkerType
import dev.auguste.agni_api.core.entities.enums.InvoiceMouvementType
import dev.auguste.agni_api.core.entities.enums.InvoiceStatusType
import dev.auguste.agni_api.core.entities.enums.InvoiceType
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.invoices.dto.TransferInvoiceInput
import dev.auguste.agni_api.core.value_objects.InvoiceModuleLinker
import java.util.UUID

class TransferInvoice(
    private val invoiceRepo: IRepository<Invoice>,
    private val accountRepo: IRepository<Account>,
    private val transactionRepo: IRepository<Transaction>,
    private val unitOfWork: IUnitOfWork
): IUseCase<TransferInvoiceInput, Unit> {
    override fun execAsync(input: TransferInvoiceInput) {
        unitOfWork.execute {
            val accountFrom = accountRepo.get(input.accountIdFrom) ?: throw DomainException.NotFound.Account(input.accountIdFrom)
            val accountTo = accountRepo.get(input.accountIdTo) ?: throw DomainException.NotFound.Account(input.accountIdTo)

            if (input.amount < 0)
                throw DomainException.BusinessLogic.Validation("Amount must be non-negative")

            val invoiceFromId = UUID.randomUUID()
            val invoiceToId = UUID.randomUUID()

            val invoiceFromModuleLinkers = input.moduleSourcesLinker.toMutableList()
                invoiceFromModuleLinkers.add(InvoiceModuleLinker(invoiceToId, InvoiceModuleLinkerType.TRANSFER))
            val invoiceToModuleLinkers = input.moduleSourcesLinker.toMutableList()
                invoiceToModuleLinkers.add(InvoiceModuleLinker(invoiceFromId, InvoiceModuleLinkerType.TRANSFER))

            val invoiceFrom = Invoice(
                id=invoiceFromId,
                accountId = accountFrom.id,
                status = InvoiceStatusType.COMPLETED,
                date = input.date,
                type = InvoiceType.OTHER,
                mouvementType = InvoiceMouvementType.DEBIT,
                moduleLinkers = invoiceFromModuleLinkers
            )

            val invoiceTo = Invoice(
                id=invoiceToId,
                accountId = accountTo.id,
                status = InvoiceStatusType.COMPLETED,
                date = input.date,
                type = InvoiceType.OTHER,
                mouvementType = InvoiceMouvementType.CREDIT,
                moduleLinkers = invoiceToModuleLinkers
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