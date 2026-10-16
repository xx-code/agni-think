package usecases.invoices

import adapters.IAuthenticateAdapter
import adapters.repositories.IRepository
import adapters.repositories.IUnitOfWork
import domain.SAVING_CATEGORY_ID
import domain.entities.Fund
import domain.entities.Invoice
import domain.enums.InvoiceModuleLinkerType
import domain.enums.InvoiceStatusType
import domain.exceptions.NotFoundException
import domain.exceptions.ValidationException
import usecases.UseCase
import usecases.funds.dto.IncreaseFundInput
import usecases.interfaces.IUseCase
import usecases.invoices.dto.DeleteInvoiceInput
import usecases.invoices.transactions.dto.GetInvoiceTransactionsInput
import usecases.invoices.transactions.dto.GetInvoiceTransactionsOutput
import java.util.UUID

class CompleteInvoiceFund(
    private val invoiceRepo: IRepository<Invoice>,
    private val getInvoiceTransactions: IUseCase<GetInvoiceTransactionsInput, List<GetInvoiceTransactionsOutput>>,
    private val increaseFund: IUseCase<IncreaseFundInput, Unit>,
    private val deleteInvoice: IUseCase<DeleteInvoiceInput, Unit>,
    auth: IAuthenticateAdapter,
    uow: IUnitOfWork
): UseCase<UUID, Unit>(uow, auth) {
    override suspend fun process(input: UUID) {
        val invoice = invoiceRepo.get(input) ?: throw NotFoundException.SingleEntity(input, "invoice")

        val invoiceTransaction = getInvoiceTransactions.processDirect(GetInvoiceTransactionsInput(
            invoiceIds = setOf(invoice.id),
            categoryIds = null,
            tagIds = null,
            budgetIds = null,
            minAmount = null,
            maxAmount = null
        )).firstOrNull()

        if (invoiceTransaction == null)
            throw ValidationException.TransactionsMustNotBeEmpty()

        if (invoice.status == InvoiceStatusType.PENDING && invoiceTransaction.transactions.first().category.id == SAVING_CATEGORY_ID) {

            val module = invoice.moduleLinkers.firstOrNull { it.module == InvoiceModuleLinkerType.FUND }

            if (module == null)
                throw ValidationException.CompleteInvoiceFundInvalidNotFund()

            increaseFund.processDirect(IncreaseFundInput(
                userId = invoice.userId,
                fundId = module.sourceId,
                accountId = invoice.accountId,
                amount = invoiceTransaction.total
            ))

            deleteInvoice.processDirect(DeleteInvoiceInput(
                invoiceId = invoice.id,
            ))
        }
    }
}