package usecases.invoices.transactions

import usecases.UseCase
import domain.SAVING_CATEGORY_ID
import domain.TRANSFERT_CATEGORY_ID
import adapters.dto.QueryFilter
import adapters.repositories.IRepository
import adapters.repositories.QueryExtendBuilder
import adapters.repositories.QueryComparator
import domain.entities.Budget
import domain.entities.Category
import domain.entities.Deduction
import domain.entities.Invoice
import domain.entities.Tag
import domain.entities.Transaction
import domain.enums.DeductionBaseType
import domain.enums.DeductionModeType
import usecases.invoices.transactions.dto.GetInvoiceTransactionsInput
import usecases.invoices.transactions.dto.GetInvoiceTransactionsOutput
import usecases.invoices.transactions.dto.TransactionBudgetOutput
import usecases.invoices.transactions.dto.TransactionCategoryOutput
import usecases.invoices.transactions.dto.TransactionOutput
import usecases.invoices.transactions.dto.TransactionTagOutput
import java.util.UUID

class GetInvoiceTransactions(
    private val invoiceRepo: IRepository<Invoice>,
    private val deductionRepo: IRepository<Deduction>,
    private val categoryRepo: IRepository<Category>,
    private val tagRepo: IRepository<Tag>,
    private val budgetRepo: IRepository<Budget>,
    private val transactionRepo: IRepository<Transaction>
): UseCase<GetInvoiceTransactionsInput, List<GetInvoiceTransactionsOutput>>() {
     override suspend fun process(input: GetInvoiceTransactionsInput): List<GetInvoiceTransactionsOutput> {
         val conditionTransactionExtend = QueryExtendBuilder<Transaction>()
             .addCondition("categoryId", QueryComparator.In, input.categoryIds)
             .addCondition("tagIds", QueryComparator.In, input.tagIds)
             .addCondition("budgetIds", QueryComparator.In, input.budgetIds)
             .addCondition("amount", QueryComparator.GreaterOrEquals, input.minAmount)
             .addCondition("amount", QueryComparator.LesserOrEquals, input.maxAmount)


         val invoices = invoiceRepo.getManyByIds(input.invoiceIds)
         val deductionIds = invoices.flatMap { invoice -> invoice.deductions }.map { it.deductionId }.toSet()
         val deductions = deductionRepo.getManyByIds(deductionIds)
         val conditionTransaction = QueryExtendBuilder<Transaction>()
             .addCondition("invoiceId", QueryComparator.In, input.invoiceIds)
         var transactions = transactionRepo.getAll(QueryFilter.queryAll(), conditionTransaction).items


         val categories = categoryRepo.getManyByIds(transactions.map { it.categoryId }.toSet())
         val tags = tagRepo.getManyByIds(transactions.flatMap { it.tagIds }.toSet())
         val budgets = budgetRepo.getManyByIds(transactions.flatMap { it.budgetIds }.toSet())

         if (input.doRemoveSpecialCategory == true) {
             transactions = transactions.filter { !setOf(SAVING_CATEGORY_ID, TRANSFERT_CATEGORY_ID).contains(it.categoryId) }
         }

         val results = mutableListOf<GetInvoiceTransactionsOutput>()

         for(invoice in invoices) {
            var invoiceTransaction = formatInvoiceTransaction(
                invoice,
                transactions,
                deductions,
                categories,
                tags,
                budgets
            )

            // Adjust Transaction, Total and Subtotal Invoice
            if (conditionTransactionExtend.getConditions().isNotEmpty()) {
               invoiceTransaction = formatInvoiceTransaction(
                   invoice,
                   transactions.filter { conditionTransactionExtend.satisfy(it) },
                   deductions,
                   categories,
                   tags,
                   budgets,
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
        categories: List<Category>,
        tags: List<Tag>,
        budgets: List<Budget>,
        invoiceParentSubtotal: Double? = null,
        invoiceParentTotal: Double? = null) : GetInvoiceTransactionsOutput {
        val transactions = transactions.filter { it.invoiceId == invoice.id }
        if (transactions.isEmpty())
            return GetInvoiceTransactionsOutput(
                invoiceId = invoice.id,
                total = 0.0,
                subTotal = 0.0,
                transactions = emptyList(),
            )

        val subTotal = transactions.sumOf { transaction -> transaction.amount }
        val invoiceDeductions = deductions.filter { deduction -> invoice.deductions.map { it.deductionId }.contains(deduction.id) }

        val deductionSubTotal = invoiceDeductions.filter { it.base == DeductionBaseType.SUBTOTAL }
        val deductionTotal = invoiceDeductions.filter { it.base == DeductionBaseType.TOTAL }

        val totalBeforeSubTotal = computeInvoiceAmountWithDeduction(subTotal, invoice, deductionSubTotal, invoiceParentSubtotal)
        val total = computeInvoiceAmountWithDeduction(totalBeforeSubTotal, invoice, deductionTotal, invoiceParentTotal)

        val getCategory = { id: UUID ->
            categories.find { it.id == id }?.let {
                TransactionCategoryOutput(
                    it.id,
                    it.title + if (it.isArchived) " (Archiver)" else "",
                    it.icon,
                    it.color.toString()
                )
            } ?:TransactionCategoryOutput(UUID.randomUUID(), "", "", "")
        }

        val getTag = { id: UUID ->
            tags.find { it.id == id }?.let {
                TransactionTagOutput(
                    it.id,
                    it.value + if (it.isArchived) " (Archiver)" else "",
                    it.color.toString()
                )
            } ?: TransactionTagOutput(UUID.randomUUID(), "", "")
        }

        val getBudget = { id: UUID ->
            budgets.find { it.id == id }?.let {
                TransactionBudgetOutput(it.id, it.title)
            } ?: TransactionBudgetOutput(UUID.randomUUID(), "")
        }

        return GetInvoiceTransactionsOutput(
            invoiceId = invoice.id,
            total = total,
            subTotal = subTotal,
            transactions = transactions.map {
                TransactionOutput(
                    id = it.id,
                    description = it.description,
                    category = getCategory(it.categoryId),
                    tags = it.tagIds.map { tagId -> getTag(tagId) }.toSet(),
                    budgets = it.budgetIds.map { budgetId -> getBudget(budgetId) }.toSet(),
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
            parentTotalAmount * (deductionAmount / parentTotalAmount)
        } ?: deductionAmount
    }
}