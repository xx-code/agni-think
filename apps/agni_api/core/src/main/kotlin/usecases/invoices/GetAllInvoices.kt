package usecases.invoices

import adapters.dto.QueryFilter
import adapters.readers.IInvoiceTransactionReader
import adapters.repositories.IQueryExtendBuilder
import adapters.repositories.IRepository
import adapters.repositories.QueryExtendBuilder
import adapters.repositories.QueryComparator
import domain.entities.Deduction
import domain.entities.Invoice
import domain.entities.Transaction
import usecases.ListOutput
import usecases.interfaces.IUseCase
import usecases.invoices.dto.GetAllInvoiceInput
import usecases.invoices.dto.GetInvoiceOutput
import usecases.invoices.dto.InvoiceDeductionOutput
import usecases.invoices.dto.InvoiceModuleLinkerOutput
import usecases.invoices.transactions.dto.GetInvoiceTransactionsInput
import usecases.invoices.transactions.dto.GetInvoiceTransactionsOutput
import domain.enums.DeductionBaseType
import domain.enums.DeductionModeType

class GetAllInvoices(
    private val invoiceRepo: IRepository<Invoice>,
    private val deductionRepo: IRepository<Deduction>,
    private val invoiceTransactionReader: IInvoiceTransactionReader,
    private val getInvoiceTransactions: IUseCase<GetInvoiceTransactionsInput, List<GetInvoiceTransactionsOutput>>
): IUseCase<GetAllInvoiceInput, ListOutput<GetInvoiceOutput>> {

    override fun execAsync(input: GetAllInvoiceInput ): ListOutput<GetInvoiceOutput> {
        input.queryFilter.sortBy.by = "date"

        val conditionInvoice = QueryExtendBuilder<Invoice>()
            .addCondition("accountId", QueryComparator.In, input.accountIds)
            .addCondition("date", QueryComparator.GreaterOrEquals, input.startDate)
            .addCondition("date", QueryComparator.LesserOrEquals, input.endDate)
            .addCondition("types", QueryComparator.In, input.types?.map { it.value }?.toSet())
            .addCondition("isFreeze", QueryComparator.Equal, input.isFreeze)
            .addCondition("status", QueryComparator.Equal, input.status?.value)
            .addCondition("movementType", QueryComparator.Equal, input.movementType)

        val conditionTransaction = QueryExtendBuilder<Transaction>()
            .addCondition("categoryId", QueryComparator.In, input.categoryIds)
            .addCondition("tagIds", QueryComparator.In, input.tagIds)
            .addCondition("budgetIds", QueryComparator.In, input.budgetIds)
            .addCondition("amount", QueryComparator.GreaterOrEquals, input.minAmount)
            .addCondition("amount", QueryComparator.LesserOrEquals, input.maxAmount)

        val deductions = deductionRepo.getAll(QueryFilter.queryAll())

        if (conditionTransaction.getConditions().isEmpty()) {
            return getInvoiceWithoutTransactionFilter(input.queryFilter, conditionInvoice, deductions.items)
        }

        return getInvoiceWithTransactionFilter(input, conditionInvoice, conditionTransaction, deductions.items)
    }

    private fun getInvoiceWithoutTransactionFilter(query: QueryFilter, queryInvoiceExtend: IQueryExtendBuilder<Invoice>, deductions: List<Deduction>) : ListOutput<GetInvoiceOutput> {
        val invoices = invoiceRepo.getAll(query, queryInvoiceExtend)

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

    private fun getInvoiceWithTransactionFilter (query: GetAllInvoiceInput, queryInvoiceExtend: IQueryExtendBuilder<Invoice>, queryTransactionExtend: IQueryExtendBuilder<Transaction>, deductions: List<Deduction>) : ListOutput<GetInvoiceOutput> {
        val invoices = invoiceTransactionReader.filteredInvoiceIds(query.queryFilter, queryInvoiceExtend, queryTransactionExtend)
        val invoiceTransactions = getInvoiceTransactions.execAsync(GetInvoiceTransactionsInput(
            invoiceIds = invoices.items.map { it.id }.toSet(),
        ))

        val results = mutableListOf<GetInvoiceOutput>()
        for (invoice in invoices.items) {
            val invoiceTransactions = invoiceTransactions.find { it.invoiceId == invoice.id }
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
            total = invoices.total
        )
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