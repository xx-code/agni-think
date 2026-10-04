package usecases.invoices

import adapters.dto.AccountSnapshotBalanceInput
import adapters.repositories.IAccountBalanceSnapshotRepository
import domain.TRANSFERT_CATEGORY_ID
import adapters.repositories.IRepository
import adapters.repositories.IUnitOfWork
import domain.entities.Account
import domain.exceptions.NotFoundException
import domain.exceptions.ValidationException
import domain.entities.Invoice
import domain.entities.Transaction
import domain.enums.InvoiceModuleLinkerType
import domain.enums.InvoiceMovementType
import domain.enums.InvoiceStatusType
import domain.enums.InvoiceType
import usecases.invoices.dto.TransferInvoiceInput
import domain.value_objects.InvoiceModuleLinker
import usecases.UseCase
import java.time.LocalDateTime
import java.util.UUID

class TransferInvoice(
    private val invoiceRepo: IRepository<Invoice>,
    private val accountRepo: IRepository<Account>,
    private val transactionRepo: IRepository<Transaction>,
    private val snapshotAccountBalanceRepo: IAccountBalanceSnapshotRepository,
    unitOfWork: IUnitOfWork
): UseCase<TransferInvoiceInput, Unit>(unitOfWork) {
    override suspend fun process(input: TransferInvoiceInput) {
        val accountFrom = accountRepo.get(input.accountIdFrom) ?: throw NotFoundException.SingleEntity(input.accountIdFrom, "account")
        val accountTo = accountRepo.get(input.accountIdTo) ?: throw NotFoundException.SingleEntity(input.accountIdTo, "account")

        if (input.amount < 0)
            throw ValidationException.AmountMustBeNonNegative()

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
            movementType = InvoiceMovementType.DEBIT,
            moduleLinkers = invoiceFromModuleLinkers
        )

        val invoiceTo = Invoice(
            id=invoiceToId,
            accountId = accountTo.id,
            status = InvoiceStatusType.COMPLETED,
            date = input.date,
            type = InvoiceType.OTHER,
            movementType = InvoiceMovementType.CREDIT,
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

        snapshotAccountBalanceRepo.makeManySnapshots(
            listOf(
                AccountSnapshotBalanceInput(accountFrom.id, accountFrom.balance, LocalDateTime.now()),
                AccountSnapshotBalanceInput(accountTo.id, accountTo.balance, LocalDateTime.now())
            )
        )
    }
}