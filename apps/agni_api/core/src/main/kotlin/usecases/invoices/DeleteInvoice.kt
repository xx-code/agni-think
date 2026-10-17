package usecases.invoices

import adapters.dto.AccountSnapshotBalanceInput
import usecases.interfaces.IUseCase

import adapters.IAuthenticateAdapter
import domain.TRANSFERT_CATEGORY_ID
import adapters.dto.QueryFilter
import adapters.events.EventType
import adapters.events.IEventRegister
import adapters.events.contents.DeleteEmbeddingInvoiceEventContent
import adapters.repositories.IAccountBalanceSnapshotRepository
import adapters.repositories.IRepository
import adapters.repositories.IUnitOfWork
import adapters.repositories.QueryExtendBuilder
import adapters.repositories.QueryComparator
import domain.entities.Account
import domain.entities.Fund
import domain.exceptions.NotFoundException
import domain.exceptions.ValidationException
import domain.entities.Invoice
import domain.entities.Transaction
import domain.enums.InvoiceModuleLinkerType
import usecases.invoices.dto.DeleteInvoiceInput
import usecases.invoices.transactions.dto.GetInvoiceTransactionsInput
import usecases.invoices.transactions.dto.GetInvoiceTransactionsOutput
import domain.enums.InvoiceMovementType
import domain.enums.InvoiceStatusType
import domain.value_objects.InvoiceModuleLinker
import usecases.UseCase
import java.time.LocalDateTime

class DeleteInvoice(
    private val invoiceRepo: IRepository<Invoice>,
    private val transactionRepo: IRepository<Transaction>,
    private val accountRepo: IRepository<Account>,
    private val getInvoiceTransactions: IUseCase<GetInvoiceTransactionsInput, List<GetInvoiceTransactionsOutput>>,
    private val snapshotAccountBalanceRepo: IAccountBalanceSnapshotRepository,
    private val eventRegister: IEventRegister,
    private val fundRepo: IRepository<Fund>,
    private val updateFundScheduleInvoice: IUseCase<Fund, Unit>,
    unitOfWork: IUnitOfWork,
    auth: IAuthenticateAdapter
): UseCase<DeleteInvoiceInput, Unit>(unitOfWork = unitOfWork, auth = auth) {

    override suspend fun process(input: DeleteInvoiceInput): Unit {
        val invoice = invoiceRepo.get(input.invoiceId) ?: throw NotFoundException.SingleEntity(input.invoiceId, "invoice")
        val account = accountRepo.get(invoice.accountId) ?: throw NotFoundException.SingleEntity(invoice.accountId, "account")

        val invoiceTransactions = getInvoiceTransactions.processDirect(GetInvoiceTransactionsInput(
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


        if (input.checkSinkingFund) {
            val fundIds = invoice.moduleLinkers.filter { it.module == InvoiceModuleLinkerType.FUND }.map { it.sourceId }
            if (fundIds.isNotEmpty()) {
                val funds = fundRepo.getManyByIds(fundIds.toSet())
                for (fund in funds) {
                    if (invoice.status == InvoiceStatusType.COMPLETED) {
                        if (invoice.movement == InvoiceMovementType.CREDIT)
                            fund.balance += invoiceTransactions.first().total
                        else
                            fund.balance -= invoiceTransactions.first().total

                        fundRepo.update(fund)
                    }

                    updateFundScheduleInvoice.execute(fund)
                }
            }
        }

        if (invoice.status == InvoiceStatusType.COMPLETED) {
            if (invoice.movement == InvoiceMovementType.CREDIT)
                account.balance -= invoiceTransactions.first().total
            else
                account.balance += invoiceTransactions.first().total

            accountRepo.update(account)

            snapshotAccountBalanceRepo.makeSnapshot(
                AccountSnapshotBalanceInput(account.userId,account.id, account.balance, LocalDateTime.now())
            )
        }

        if (invoice.status == InvoiceStatusType.COMPLETED)
            eventRegister.notify(EventType.DELETE_INVOICE, DeleteEmbeddingInvoiceEventContent(input.invoiceId))

    }
}