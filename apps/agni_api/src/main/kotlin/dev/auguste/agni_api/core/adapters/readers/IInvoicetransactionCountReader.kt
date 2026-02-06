package dev.auguste.agni_api.core.adapters.readers

import dev.auguste.agni_api.core.adapters.repositories.IQueryExtend
import dev.auguste.agni_api.core.entities.Invoice
import dev.auguste.agni_api.core.entities.Transaction

interface IInvoicetransactionCountReader {
    fun count(queryInvoiceExtend: IQueryExtend<Invoice>, queryTransactionExtend: IQueryExtend<Transaction>): Long
}