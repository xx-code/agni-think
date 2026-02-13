package dev.auguste.agni_api.core.usecases.invoices.transactions

import dev.auguste.agni_api.core.SAVING_CATEGORY_ID
import dev.auguste.agni_api.core.TRANSFERT_CATEGORY_ID
import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.repositories.IRepository
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
        var transactions = transactionRepo.getAll(QueryFilter(0, 0, true), extends).items

         if (input.doRemoveSpecialCategory == true) {
         }

        val results = mutableListOf<GetInvoiceTransactionsOutput>()

        for(invoice in invoices) {
            val transactions = transactions.filter { it.invoiceId == invoice.id }
            if (transactions.isNotEmpty()) {
                val subTotal = transactions.sumOf { transaction -> transaction.amount }
                val invoiceDeductions = deductions.filter { deduction -> invoice.deductions.map { it.deductionId }.contains(deduction.id) }

                val deductionSubTotal = invoiceDeductions.filter { it.base == DeductionBaseType.SUBTOTAL }
                val deductionTotal = invoiceDeductions.filter { it.base == DeductionBaseType.TOTAL }

                val totalBeforeSubTotal = subTotal + deductionSubTotal.sumOf { deduction ->
                    val invoiceDeduction = invoice.deductions.find { it.deductionId == deduction.id }
                    invoiceDeduction?.let {
                        if (deduction.mode == DeductionModeType.FLAT)
                            it.amount
                        else
                            subTotal * (it.amount / 100)
                    } ?: 0.0
                }

                val total = totalBeforeSubTotal + deductionTotal.sumOf { deduction ->
                    val invoiceDeduction = invoice.deductions.find { it.deductionId == deduction.id }
                    invoiceDeduction?.let {
                        if (deduction.mode == DeductionModeType.FLAT)
                            it.amount
                        else
                            totalBeforeSubTotal * (it.amount / 100)
                    } ?: 0.0
                }

                results.add(GetInvoiceTransactionsOutput(
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
                ))
            }
        }

        return results
    }
}