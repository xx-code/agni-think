package dev.auguste.agni_api.core.usecases.invoices

import dev.auguste.agni_api.core.adapters.readers.IInvoicetransactionCountReader
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryInvoiceExtend
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryTransactionExtend
import dev.auguste.agni_api.core.entities.Invoice
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.invoices.dto.GetAllInvoiceInput
import dev.auguste.agni_api.core.usecases.invoices.dto.GetInvoiceOutput
import dev.auguste.agni_api.core.usecases.invoices.transactions.dto.GetInvoiceTransactionsInput
import dev.auguste.agni_api.core.usecases.invoices.transactions.dto.GetInvoiceTransactionsOutput

class GetAllInvoices(
    val invoiceRepo: IRepository<Invoice>,
    val invoiceTransactionCountReader: IInvoicetransactionCountReader,
    val getInvoiceTransactions: IUseCase<GetInvoiceTransactionsInput, ListOutput<GetInvoiceTransactionsOutput>>
): IUseCase<GetAllInvoiceInput, ListOutput<GetInvoiceOutput>> {

    override fun execAsync(input: GetAllInvoiceInput ): ListOutput<GetInvoiceOutput> {
        val queryInvoiceExtend = QueryInvoiceExtend(
            accountIds = input.accountIds,
            startDate = input.startDate,
            endDate = input.endDate,
            type = input.type,
            isFreeze = input.isFreeze,
            status = input.status,
            mouvementType = input.mouvementType
        )

        val invoices = invoiceRepo.getAll(input.queryFilter, queryInvoiceExtend )
        val queryTransactionExtend = QueryTransactionExtend(
            invoiceIds = invoices.items.map { it.id }.toSet(),
            categoryIds = input.categoryIds,
            tagIds = input.tagIds,
            budgetIds = input.budgetIds,
            minAmount = input.minAmount,
            maxAmount = input.maxAmount
        )
        val totalItems = invoiceTransactionCountReader.count(queryInvoiceExtend, queryTransactionExtend)

        val results = mutableListOf<GetInvoiceOutput>()

        getInvoiceTransactions.execAsync(GetInvoiceTransactionsInput(
            invoiceIds = queryTransactionExtend.invoiceIds!!,
            categoryIds = queryTransactionExtend.categoryIds,
            tagIds = queryTransactionExtend.tagIds,
            budgetIds = queryTransactionExtend.budgetIds,
            minAmount = queryTransactionExtend.minAmount,
            maxAmount = queryTransactionExtend.maxAmount
        ))

        return ListOutput(
            items = results,
            total = totalItems
        )
    }
}