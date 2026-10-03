package adapters.readers

import adapters.dto.QueryFilter
import adapters.repositories.IQueryExtendBuilder
import domain.entities.Invoice
import domain.entities.Transaction
import usecases.dto.ListOutput

interface IInvoiceTransactionReader {
    fun count(queryInvoiceExtend: IQueryExtendBuilder<Invoice>, queryTransactionExtend: IQueryExtendBuilder<Transaction>): Long
    fun filteredInvoiceIds(query: QueryFilter, queryInvoiceExtend: IQueryExtendBuilder<Invoice>, queryTransactionExtend: IQueryExtendBuilder<Transaction>): ListOutput<Invoice>
}