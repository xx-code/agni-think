package dev.auguste.agni_api.core.adapters.readers

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.repositories.IQueryExtend
import dev.auguste.agni_api.core.entities.Invoice
import dev.auguste.agni_api.core.entities.Transaction
import dev.auguste.agni_api.core.usecases.ListOutput

interface IInvoicetransactionCountReader {
    fun count(queryInvoiceExtend: IQueryExtend<Invoice>, queryTransactionExtend: IQueryExtend<Transaction>): Long
    fun pagination(query: QueryFilter, queryInvoiceExtend: IQueryExtend<Invoice>, queryTransactionExtend: IQueryExtend<Transaction>): ListOutput<Invoice>
}