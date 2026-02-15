package dev.auguste.agni_api.core.usecases.invoices

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.readers.IInvoicetransactionCountReader
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryInvoiceExtend
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryTransactionExtend
import dev.auguste.agni_api.core.entities.Deduction
import dev.auguste.agni_api.core.entities.Invoice
import dev.auguste.agni_api.core.entities.enums.DeductionBaseType
import dev.auguste.agni_api.core.entities.enums.DeductionModeType
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.invoices.dto.GetAllInvoiceInput
import dev.auguste.agni_api.core.usecases.invoices.dto.GetBalanceOutput
import dev.auguste.agni_api.core.usecases.invoices.dto.GetInvoiceOutput
import dev.auguste.agni_api.core.usecases.invoices.dto.InvoiceDeductionOutput
import dev.auguste.agni_api.core.usecases.invoices.transactions.dto.GetInvoiceTransactionsInput
import dev.auguste.agni_api.core.usecases.invoices.transactions.dto.GetInvoiceTransactionsOutput
import dev.auguste.agni_api.core.value_objects.InvoiceDeduction

class GetAllInvoices(
    private val invoiceRepo: IRepository<Invoice>,
    private val deductionRepo: IRepository<Deduction>,
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

        val queryTransactionExtend = QueryTransactionExtend(
            invoiceIds = null,
            categoryIds = input.categoryIds,
            tagIds = input.tagIds,
            budgetIds = input.budgetIds,
            minAmount = input.minAmount,
            maxAmount = input.maxAmount
        )

        val deductions = deductionRepo.getAll(QueryFilter(0, 0, true))

        if (!haveTransactionsFilter(queryTransactionExtend)) {
            return getInvoiceWithoutTransactionFilter(input.queryFilter, queryInvoiceExtend, deductions.items)
        }

        return getInvoiceWithTransactionFilter(input.queryFilter, queryInvoiceExtend,queryTransactionExtend, deductions.items)
    }

    private fun getInvoiceWithoutTransactionFilter(query: QueryFilter, queryInvoiceExtend: QueryInvoiceExtend, deductions: List<Deduction>) : ListOutput<GetInvoiceOutput> {
        val invoices = invoiceRepo.getAll(query, queryInvoiceExtend) // invoiceTransactionCountReader.pagination(input.queryFilter, queryInvoiceExtend, queryTransactionExtend)

        val results = mutableListOf<GetInvoiceOutput>()

        val transactions = getInvoiceTransactions.execAsync(GetInvoiceTransactionsInput(
            invoiceIds = invoices.items.map { it.id }.toSet(),
            categoryIds = null,
            tagIds = null,
            budgetIds = null,
            minAmount = null,
            maxAmount = null
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
                        deductions = formatInvoiceDeductionsOutput(invoice, invoiceTransactions, deductions)
                    )
                )
            }
        }

        return ListOutput(
            items = results,
            total = invoices.total,
        )
    }

    private fun getInvoiceWithTransactionFilter (query: QueryFilter, queryInvoiceExtend: QueryInvoiceExtend, queryTransactionExtend: QueryTransactionExtend, deductions: List<Deduction>) : ListOutput<GetInvoiceOutput> {
        val response = invoiceTransactionCountReader.filteredInvoiceIds(query, queryInvoiceExtend, queryTransactionExtend)
        val invoices = invoiceRepo.getManyByIds(response.items.toSet())

        val transactions = getInvoiceTransactions.execAsync(GetInvoiceTransactionsInput(
            invoiceIds = response.items.toSet(),
            categoryIds = queryTransactionExtend.categoryIds,
            tagIds = queryTransactionExtend.tagIds,
            budgetIds = queryTransactionExtend.budgetIds,
            minAmount = queryTransactionExtend.minAmount,
            maxAmount = queryTransactionExtend.maxAmount
        ))
        val results = mutableListOf<GetInvoiceOutput>()
        for (invoice in invoices) {
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
                        deductions = formatInvoiceDeductionsOutput(invoice, invoiceTransactions, deductions)
                    )
                )
            }
        }

        return ListOutput(
            items = results,
            total = response.total
        )
    }

    private fun haveTransactionsFilter(query: QueryTransactionExtend) : Boolean {
        return query.categoryIds != null || query.tagIds != null || query.budgetIds != null || query.minAmount != null
                || query.maxAmount != null
    }

    private fun formatInvoiceDeductionsOutput(invoice: Invoice, invoiceTransactions: GetInvoiceTransactionsOutput, deductions: List<Deduction>) : List<InvoiceDeductionOutput> {
        val invoiceDeductions = mutableListOf<InvoiceDeductionOutput>()

        for (invoiceDeduction in invoice.deductions) {
            val deduction = deductions.find { it.id ==  invoiceDeduction.deductionId }
            if (deduction != null) {
                val amount = if (deduction.base == DeductionBaseType.SUBTOTAL) {
                    invoiceTransactions.subTotal
                } else{
                    invoiceTransactions.total
                }

                val res = if (deduction.mode == DeductionModeType.FLAT) {
                    invoiceDeduction.amount
                } else {
                    amount * (invoiceDeduction.amount / 100)
                }

                invoiceDeductions.add(InvoiceDeductionOutput(
                    id = invoiceDeduction.deductionId,
                    amount = res
                ))
            }
        }

        return invoiceDeductions
    }
}