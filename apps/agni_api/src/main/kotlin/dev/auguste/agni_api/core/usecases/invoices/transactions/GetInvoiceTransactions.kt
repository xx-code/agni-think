package dev.auguste.agni_api.core.usecases.invoices.transactions

import dev.auguste.agni_api.core.SAVING_CATEGORY_ID
import dev.auguste.agni_api.core.TRANSFERT_CATEGORY_ID
import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryInvoiceExtend
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryTransactionExtend
import dev.auguste.agni_api.core.entities.Deduction
import dev.auguste.agni_api.core.entities.Invoice
import dev.auguste.agni_api.core.entities.Transaction
import dev.auguste.agni_api.core.entities.enums.DeductionBaseType
import dev.auguste.agni_api.core.entities.enums.DeductionModeType
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.invoices.transactions.dto.GetInvoiceTransactionsInput
import dev.auguste.agni_api.core.usecases.invoices.transactions.dto.GetInvoiceTransactionsOutput
import dev.auguste.agni_api.core.usecases.invoices.transactions.dto.TransactionOutput

class GetInvoiceTransactions(
    private val invoiceRepo: IRepository<Invoice>,
    private val deductionRepo: IRepository<Deduction>,
    private val transactionRepo: IRepository<Transaction>
): IUseCase<GetInvoiceTransactionsInput, List<GetInvoiceTransactionsOutput>> {
     override fun execAsync(input: GetInvoiceTransactionsInput): List<GetInvoiceTransactionsOutput> {
        val extends = QueryTransactionExtend(
            invoiceIds = input.invoiceIds,
            tagIds = input.tagIds,
            categoryIds = input.categoryIds,
            budgetIds = input.budgetIds,
            maxAmount = input.maxAmount,
            minAmount = input.minAmount
        )

        val invoices = invoiceRepo.getManyByIds(input.invoiceIds)
        val deductionIds = invoices.flatMap { invoice -> invoice.deductions }.map { it.deductionId }.toSet()
        val deductions = deductionRepo.getManyByIds(deductionIds)
        var transactions = transactionRepo.getAll(
            QueryFilter(0, 0, true),
            QueryTransactionExtend(
                invoiceIds = input.invoiceIds,
                tagIds = null,
                categoryIds = null,
                budgetIds = null,
                maxAmount = null,
                minAmount = null
            )).items

         if (input.doRemoveSpecialCategory == true) {
             transactions = transactions.filter { !setOf(SAVING_CATEGORY_ID, TRANSFERT_CATEGORY_ID).contains(it.categoryId) }
         }

        val results = mutableListOf<GetInvoiceTransactionsOutput>()

        for(invoice in invoices) {
            var invoiceTransaction = formatInvoiceTransaction(invoice, transactions, deductions)

            // Adjust Transaction, Total and Subtotal Invoice
            if (doFilterTransactions(extends)) {
               invoiceTransaction = formatInvoiceTransaction(
                   invoice,
                   transactions.filter { extends.isStatisfy(it) },
                   deductions,
                   invoiceTransaction.subTotal,
                   invoiceTransaction.total
               )
            }

            results.add(invoiceTransaction)
        }

        return results
    }

    private fun formatInvoiceTransaction(
        invoice: Invoice,
        transactions: List<Transaction>,
        deductions: List<Deduction>,
        invoiceParentSubtotal: Double? = null,
        invoiceParentTotal: Double? = null) : GetInvoiceTransactionsOutput {
        val transactions = transactions.filter { it.invoiceId == invoice.id }
        if (transactions.isEmpty())
            return GetInvoiceTransactionsOutput(
                invoiceId = invoice.id,
                total = 0.0,
                subTotal = 0.0,
                transactions = emptyList()
            )

        val subTotal = transactions.sumOf { transaction -> transaction.amount }
        val invoiceDeductions = deductions.filter { deduction -> invoice.deductions.map { it.deductionId }.contains(deduction.id) }

        val deductionSubTotal = invoiceDeductions.filter { it.base == DeductionBaseType.SUBTOTAL }
        val deductionTotal = invoiceDeductions.filter { it.base == DeductionBaseType.TOTAL }

        val totalBeforeSubTotal = computeInvoiceAmountWithDeduction(subTotal, invoice, deductionSubTotal, invoiceParentSubtotal)
        val total = computeInvoiceAmountWithDeduction(totalBeforeSubTotal, invoice, deductionTotal, invoiceParentTotal)

        return GetInvoiceTransactionsOutput(
            invoiceId = invoice.id,
            total = total,
            subTotal = subTotal,
            transactions = transactions.map {
                TransactionOutput(
                    id = it.id,
                    description = it.description,
                    categoryId = it.categoryId,
                    tagIds = it.tagIds,
                    budgetIds = it.budgetIds,
                    amount = it.amount
                )
            }
        )
    }

    private fun computeInvoiceAmountWithDeduction(total: Double,  invoice: Invoice, deductions: List<Deduction>, parentInvoiceSubtotal: Double? = null) : Double {
        return total + deductions.sumOf { deduction ->
            val invoiceDeduction = invoice.deductions.find { it.deductionId == deduction.id }
            invoiceDeduction?.let {
                if (deduction.mode == DeductionModeType.FLAT)
                    adjustFlatDeductionAmountAfterFiltered(it.amount, parentInvoiceSubtotal)
                else
                    total * (it.amount / 100)
            } ?: 0.0
        }
    }

    private fun adjustFlatDeductionAmountAfterFiltered(deductionAmount: Double, parentTotalAmount: Double?=null) : Double {
        return parentTotalAmount?.let {
            parentTotalAmount + (deductionAmount * 100 / parentTotalAmount)
        } ?: deductionAmount
    }


    private fun doFilterTransactions(query: QueryTransactionExtend) : Boolean{
        return query.categoryIds !== null || query.tagIds !== null || query.budgetIds !== null || query.maxAmount !== null
                || query.minAmount !== null
    }
}