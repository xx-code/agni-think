package dev.auguste.agni_api.core.usecases.invoices

import dev.auguste.agni_api.core.TRANSFERT_CATEGORY_ID
import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.adapters.repositories.IUnitOfWork
import dev.auguste.agni_api.core.adapters.repositories.QueryExtendBuilder
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryComparator
import dev.auguste.agni_api.core.entities.DomainException
import dev.auguste.agni_api.core.entities.Invoice
import dev.auguste.agni_api.core.entities.Transaction
import dev.auguste.agni_api.core.entities.enums.InvoiceModuleLinkerType
import dev.auguste.agni_api.core.usecases.interfaces.IInnerUseCase
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.invoices.dto.DeleteInvoiceInput
import java.util.UUID

class CancelTransfer(
    private val invoiceRepo: IRepository<Invoice>,
    private val transactionRepo: IRepository<Transaction>,
    private val deleteInvoice: IInnerUseCase<DeleteInvoiceInput, Unit>,
    private val unitOfWork: IUnitOfWork
): IUseCase<UUID, Unit> {
    override fun execAsync(input: UUID) {
        unitOfWork.execute {
            val invoice = invoiceRepo.get(input) ?: throw DomainException.NotFound.Invoice(input)
            val condition = QueryExtendBuilder<Transaction>()
                .addCondition("invoiceId", QueryComparator.Equal, invoice.id)
            val transactions = transactionRepo.getAll(QueryFilter.queryAll(), condition)
            if (transactions.items.isEmpty())
                    throw DomainException.NotFound.Invoice(invoice.id)

            if (transactions.items.first().categoryId != TRANSFERT_CATEGORY_ID)
                throw DomainException.BusinessLogic.CanOnlyCancelTransfer()

            val linkedTransactionIds = invoice.moduleLinkers.filter { it.module == InvoiceModuleLinkerType.TRANSFER }.map { it.sourceId }

            deleteInvoice.execInnerAsync(DeleteInvoiceInput(invoice.id, checkTransfer = false))
            for (linkedTransactionId in linkedTransactionIds) {
                deleteInvoice.execInnerAsync(DeleteInvoiceInput(linkedTransactionId, checkTransfer = false))
            }
        }
    }
}