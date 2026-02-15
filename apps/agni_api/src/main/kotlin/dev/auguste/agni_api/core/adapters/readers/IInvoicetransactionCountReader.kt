package dev.auguste.agni_api.core.adapters.readers

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.repositories.IQueryExtend
import dev.auguste.agni_api.core.entities.Invoice
import dev.auguste.agni_api.core.entities.Transaction
import dev.auguste.agni_api.core.usecases.ListOutput
import java.util.UUID

interface IInvoicetransactionCountReader {
    fun count(queryInvoiceExtend: IQueryExtend<Invoice>, queryTransactionExtend: IQueryExtend<Transaction>): Long
    fun filteredInvoiceIds(query: QueryFilter, queryInvoiceExtend: IQueryExtend<Invoice>, queryTransactionExtend: IQueryExtend<Transaction>): ListOutput<UUID>
}