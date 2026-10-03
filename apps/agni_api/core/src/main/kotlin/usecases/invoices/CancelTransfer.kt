package usecases.invoices

import domain.TRANSFERT_CATEGORY_ID
import adapters.dto.QueryFilter
import adapters.repositories.IRepository
import adapters.repositories.IUnitOfWork
import adapters.repositories.QueryExtendBuilder
import adapters.repositories.QueryComparator
import domain.exceptions.NotFoundException
import domain.exceptions.ValidationException
import domain.entities.Invoice
import domain.entities.Transaction
import domain.enums.InvoiceModuleLinkerType
import usecases.interfaces.IInnerUseCase
import usecases.interfaces.IUseCase
import usecases.invoices.dto.DeleteInvoiceInput
import java.util.UUID

class CancelTransfer(
    private val invoiceRepo: IRepository<Invoice>,
    private val transactionRepo: IRepository<Transaction>,
    private val deleteInvoice: IInnerUseCase<DeleteInvoiceInput, Unit>,
    private val unitOfWork: IUnitOfWork
): IUseCase<UUID, Unit> {
    override fun execAsync(input: UUID) {
        unitOfWork.execute {
            val invoice = invoiceRepo.get(input) ?: throw NotFoundException.SingleEntity(input, "invoice")
            val condition = QueryExtendBuilder<Transaction>()
                .addCondition("invoiceId", QueryComparator.Equal, invoice.id)
            val transactions = transactionRepo.getAll(QueryFilter.queryAll(), condition)
            if (transactions.items.isEmpty())
                    throw NotFoundException.SingleEntity(invoice.id, "invoice")

            if (transactions.items.first().categoryId != TRANSFERT_CATEGORY_ID)
                throw ValidationException.CanOnlyCancelTransfer()

            val linkedTransactionIds = invoice.moduleLinkers.filter { it.module == InvoiceModuleLinkerType.TRANSFER }.map { it.sourceId }

            deleteInvoice.execInnerAsync(DeleteInvoiceInput(invoice.id, checkTransfer = false))
            for (linkedTransactionId in linkedTransactionIds) {
                deleteInvoice.execInnerAsync(DeleteInvoiceInput(linkedTransactionId, checkTransfer = false))
            }
        }
    }
}