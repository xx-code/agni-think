package adapters.readers

import adapters.dto.QueryFilter
import adapters.repositories.IQueryExtend
import domain.entities.Invoice
import domain.entities.Transaction
import usecases.ListOutput
import java.util.UUID

interface IInvoicetransactionCountReader {
    fun count(queryInvoiceExtend: IQueryExtend<domain.entities.Invoice>, queryTransactionExtend: IQueryExtend<domain.entities.Transaction>): Long
    fun filteredInvoiceIds(query: QueryFilter, queryInvoiceExtend: IQueryExtend<domain.entities.Invoice>, queryTransactionExtend: IQueryExtend<domain.entities.Transaction>): ListOutput<UUID>
}