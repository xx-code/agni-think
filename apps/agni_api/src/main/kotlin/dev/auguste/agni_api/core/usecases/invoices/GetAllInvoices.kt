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
import dev.auguste.agni_api.core.usecases.invoices.dto.GetInvoiceOutput
import dev.auguste.agni_api.core.usecases.invoices.dto.InvoiceDeductionOutput
import dev.auguste.agni_api.core.usecases.invoices.transactions.dto.GetInvoiceTransactionsInput
import dev.auguste.agni_api.core.usecases.invoices.transactions.dto.GetInvoiceTransactionsOutput

class GetAllInvoices(
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

        val invoices = invoiceTransactionCountReader.pagination(input.queryFilter, queryInvoiceExtend, queryTransactionExtend)

        val results = mutableListOf<GetInvoiceOutput>()

        val transactions = getInvoiceTransactions.execAsync(GetInvoiceTransactionsInput(
            invoiceIds = invoices.items.map { it.id }.toSet(),
            categoryIds = queryTransactionExtend.categoryIds,
            tagIds = queryTransactionExtend.tagIds,
            budgetIds = queryTransactionExtend.budgetIds,
            minAmount = queryTransactionExtend.minAmount,
            maxAmount = queryTransactionExtend.maxAmount
        ))

        for (invoice in invoices.items) {
            val invoiceTransactions = transactions.find { it.invoiceId == invoice.id }
            if (invoiceTransactions != null) {
                val invoiceDeductions = mutableListOf<InvoiceDeductionOutput>()

                for (invoiceDeduction in invoice.deductions) {
                    val deduction = deductions.items.find { it.id ==  invoiceDeduction.deductionId }
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
                        deductions = invoiceDeductions
                    )
                )
            }
        }

        return ListOutput(
            items = results,
            total = invoices.total,
        )
    }
}