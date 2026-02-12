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
import dev.auguste.agni_api.core.usecases.invoices.dto.InvoiceDeductionOutput
import dev.auguste.agni_api.core.usecases.invoices.transactions.dto.GetInvoiceTransactionsInput
import dev.auguste.agni_api.core.usecases.invoices.transactions.dto.GetInvoiceTransactionsOutput

class GetAllInvoices(
    private val invoiceRepo: IRepository<Invoice>,
    private val invoiceTransactionCountReader: IInvoicetransactionCountReader,
    private val getInvoiceTransactions: IUseCase<GetInvoiceTransactionsInput, List<GetInvoiceTransactionsOutput>>
): IUseCase<GetAllInvoiceInput, ListOutput<GetInvoiceOutput>> {

    override fun execAsync(input: GetAllInvoiceInput ): ListOutput<GetInvoiceOutput> {
        input.queryFilter.sortBy.by = "date"

        val queryInvoiceExtend = QueryInvoiceExtend(
            accountIds = input.accountIds,
            startDate = input.startDate,
            endDate = input.endDate,
            types = input.types,
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

        val transactions = getInvoiceTransactions.execAsync(GetInvoiceTransactionsInput(
            invoiceIds = queryTransactionExtend.invoiceIds!!,
            categoryIds = queryTransactionExtend.categoryIds,
            tagIds = queryTransactionExtend.tagIds,
            budgetIds = queryTransactionExtend.budgetIds,
            minAmount = queryTransactionExtend.minAmount,
            maxAmount = queryTransactionExtend.maxAmount
        ))

        for (invoice in invoices.items) {
            val invoiceTransactions = transactions.find { it.invoiceId == invoice.id }
            if (invoiceTransactions != null) {
                results.add(
                    GetInvoiceOutput(
                        id = invoice.id,
                        accountId = invoice.accountId,
                        status = invoice.statusType.value,
                        subTotal = invoiceTransactions.subTotal,
                        total = invoiceTransactions.total,
                        mouvement = invoice.mouvementType.value,
                        date = invoice.date,
                        type = invoice.type.value,
                        transactions = invoiceTransactions.transactions,
                        deductions = invoice.deductions.map { InvoiceDeductionOutput(
                            it.deductionId, it.amount
                        ) }
                    )
                )
            }
        }

        return ListOutput(
            items = results,
            total = totalItems
        )
    }
}