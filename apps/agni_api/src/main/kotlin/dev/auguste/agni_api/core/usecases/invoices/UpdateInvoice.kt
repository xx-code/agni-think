package dev.auguste.agni_api.core.usecases.invoices

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.adapters.repositories.IUnitOfWork
import dev.auguste.agni_api.core.entities.Invoice
import dev.auguste.agni_api.core.facades.InvoiceDependencies
import dev.auguste.agni_api.core.usecases.CreatedOutput
import dev.auguste.agni_api.core.usecases.interfaces.IInnerUseCase
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.invoices.dto.CreateInvoiceInput
import dev.auguste.agni_api.core.usecases.invoices.dto.DeleteInvoiceInput
import dev.auguste.agni_api.core.usecases.invoices.dto.InvoiceDeductionInput
import dev.auguste.agni_api.core.usecases.invoices.dto.TransactionInput
import dev.auguste.agni_api.core.usecases.invoices.dto.UpdateInvoiceInput
import dev.auguste.agni_api.core.usecases.invoices.transactions.dto.GetInvoiceTransactionsInput
import dev.auguste.agni_api.core.usecases.invoices.transactions.dto.GetInvoiceTransactionsOutput
import dev.auguste.agni_api.core.value_objects.InvoiceDeduction
import java.util.UUID

class UpdateInvoice(
    private val invoiceRepo: IRepository<Invoice>,
    private val invoiceDependencies: InvoiceDependencies,
    private val createInvoice: IInnerUseCase<CreateInvoiceInput, CreatedOutput>,
    private val deleteInvoice: IInnerUseCase<DeleteInvoiceInput, Unit>,
    private val getInvoiceTransactions: IUseCase<GetInvoiceTransactionsInput, List<GetInvoiceTransactionsOutput>>,
    private val unitOfWork: IUnitOfWork
): IUseCase<UpdateInvoiceInput, Unit> {
    override fun execAsync(
        input: UpdateInvoiceInput
    ) {
        unitOfWork.execute {
            val invoice = invoiceRepo.get(input.id) ?: throw Error("Invoice ${input.id} not found")

            if (input.accountId != null) {
                if (invoiceDependencies.accountRepo.get(input.accountId) == null)
                    throw Error("Account ${input.accountId} not found")

                invoice.accountId = input.accountId
            }

            if (input.type != null)
                invoice.type = input.type

            if (input.mouvementType != null)
                invoice.mouvementType = input.mouvementType

            if (input.date != null)
                invoice.date = input.date

            if (input.deductions != null)
                invoice.deductions = input.deductions.map { InvoiceDeduction(it.deductionId, it.amount) }.toMutableSet()

            val anyTransactionChange = input.addTransactions.isNotEmpty() || input.removeTransactionIds.isNotEmpty()

            if (invoice.hasChanged() || anyTransactionChange) {
                val transactions = input.addTransactions.ifEmpty {
                    getInvoiceTransactions.execAsync(GetInvoiceTransactionsInput(
                        invoiceIds = setOf(invoice.id),
                        categoryIds = null,
                        tagIds = null,
                        budgetIds = null,
                        minAmount = null,
                        maxAmount = null
                    )).first().transactions.map { TransactionInput(
                        amount = it.amount,
                        categoryId = it.categoryId,
                        description = it.description,
                        tagIds = it.tagIds,
                        budgetIds = it.budgetIds,
                    ) }.toSet()
                }

                createInvoice.execAsync(CreateInvoiceInput(
                    accountId = invoice.accountId,
                    status = invoice.statusType,
                    date = invoice.date,
                    type = invoice.type,
                    mouvementType = invoice.mouvementType,
                    currency = null,
                    isFreeze = invoice.isFreeze,
                    transactions = transactions,
                    deductions = invoice.deductions.map { InvoiceDeductionInput(
                        it.deductionId, it.amount
                    ) }.toSet()
                ))

                deleteInvoice.execInnerAsync(DeleteInvoiceInput(invoice.id))
            }
        }
    }

}