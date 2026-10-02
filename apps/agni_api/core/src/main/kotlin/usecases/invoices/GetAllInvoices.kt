package usecases.invoices

import adapters.dto.QueryFilter
import adapters.readers.IInvoicetransactionCountReader
import adapters.repositories.IRepository
import adapters.repositories.query_extend.QueryInvoiceExtend
import adapters.repositories.query_extend.QueryTransactionExtend
import domain.entities.Deduction
import domain.entities.Invoice
import usecases.ListOutput
import usecases.interfaces.IUseCase
import usecases.invoices.dto.GetAllInvoiceInput
import usecases.invoices.dto.GetInvoiceOutput
import usecases.invoices.dto.InvoiceDeductionOutput
import usecases.invoices.dto.InvoiceModuleLinkerOutput
import usecases.invoices.transactions.dto.GetInvoiceTransactionsInput
import usecases.invoices.transactions.dto.GetInvoiceTransactionsOutput

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
                        mouvement = invoice.movementType.value,
                        date = invoice.date,
                        isFreeze = invoice.isFreeze,
                        type = invoice.type.value,
                        transactions = invoiceTransactions.transactions,
                        deductions = formatInvoiceDeductionsOutput(invoice, invoiceTransactions, deductions),
                        moduleLinkers = invoice.moduleLinkers.map { InvoiceModuleLinkerOutput(it.sourceId, it.module.value) }
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
                        mouvement = invoice.movementType.value,
                        date = invoice.date,
                        isFreeze = invoice.isFreeze,
                        type = invoice.type.value,
                        transactions = invoiceTransactions.transactions,
                        deductions = formatInvoiceDeductionsOutput(invoice, invoiceTransactions, deductions),
                        moduleLinkers = invoice.moduleLinkers.map { InvoiceModuleLinkerOutput(it.sourceId, it.module.value) }
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
                val amount = if (deduction.base == _root_ide_package_.domain.enums.DeductionBaseType.SUBTOTAL) {
                    invoiceTransactions.subTotal
                } else{
                    invoiceTransactions.total
                }

                val res = if (deduction.mode == _root_ide_package_.domain.enums.DeductionModeType.FLAT) {
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