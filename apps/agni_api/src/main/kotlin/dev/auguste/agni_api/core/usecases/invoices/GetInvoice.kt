package dev.auguste.agni_api.core.usecases.invoices

import dev.auguste.agni_api.core.adapters.readers.IInvoicetransactionCountReader
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Invoice
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.invoices.dto.GetInvoiceOutput
import dev.auguste.agni_api.core.usecases.invoices.dto.InvoiceDeductionOutput
import dev.auguste.agni_api.core.usecases.invoices.transactions.dto.GetInvoiceTransactionsInput
import dev.auguste.agni_api.core.usecases.invoices.transactions.dto.GetInvoiceTransactionsOutput
import java.util.UUID

class GetInvoice(
    private val invoiceRepo: IRepository<Invoice>,
    private val getInvoiceTransactions: IUseCase<GetInvoiceTransactionsInput, List<GetInvoiceTransactionsOutput>>
): IUseCase<UUID, GetInvoiceOutput> {
    override fun execAsync(input: UUID): GetInvoiceOutput {
        val invoice = invoiceRepo.get(input) ?: throw Error("Invoice with id $input not found")

        val invoiceTransactions = getInvoiceTransactions.execAsync(GetInvoiceTransactionsInput(
            invoiceIds = setOf(invoice.id),
            categoryIds = null,
            tagIds = null,
            budgetIds = null,
            minAmount = null,
            maxAmount = null
        ))

        if (invoiceTransactions.isEmpty())
            throw Error("Invoice with id $input not Transactions")


        return GetInvoiceOutput(
            id = invoice.id,
            accountId = invoice.accountId,
            status = invoice.statusType,
            subTotal = invoiceTransactions.first().subTotal,
            total = invoiceTransactions.first().total,
            mouvement = invoice.mouvementType,
            date = invoice.date,
            transactions = invoiceTransactions.first().transactions,
            deductions = invoice.deductions.map { InvoiceDeductionOutput(
                it.deductionId, it.amount
            ) }
        )
    }
}