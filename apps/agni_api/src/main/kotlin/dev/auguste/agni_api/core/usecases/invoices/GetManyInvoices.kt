package dev.auguste.agni_api.core.usecases.invoices

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.DomainException
import dev.auguste.agni_api.core.entities.Invoice
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.invoices.dto.GetInvoiceOutput
import dev.auguste.agni_api.core.usecases.invoices.dto.InvoiceDeductionOutput
import dev.auguste.agni_api.core.usecases.invoices.dto.InvoiceModuleLinkerOutput
import dev.auguste.agni_api.core.usecases.invoices.transactions.dto.GetInvoiceTransactionsInput
import dev.auguste.agni_api.core.usecases.invoices.transactions.dto.GetInvoiceTransactionsOutput
import java.util.UUID

class GetManyInvoices(
    private val invoiceRepo: IRepository<Invoice>,
    private val getInvoiceTransactions: IUseCase<GetInvoiceTransactionsInput, List<GetInvoiceTransactionsOutput>>
): IUseCase<Set<UUID>, List<GetInvoiceOutput>> {
    override fun execAsync(input: Set<UUID>): List<GetInvoiceOutput> {
        val invoices = invoiceRepo.getManyByIds(input)

        val invoiceTransactions = getInvoiceTransactions.execAsync(GetInvoiceTransactionsInput(
            invoiceIds = input,
            categoryIds = null,
            tagIds = null,
            budgetIds = null,
            minAmount = null,
            maxAmount = null
        ))

        if (invoiceTransactions.isEmpty())
            throw DomainException.BusinessLogic.TransactionsMustNotBeEmpty()

        val outputInvoices = mutableListOf<GetInvoiceOutput>()

        invoiceTransactions.groupBy { it.invoiceId }.forEach { invoiceTransaction ->
            val invoice = invoices.firstOrNull( { invoiceTransaction.key == it.id } )
            if (invoice != null) {
                val transactions = invoiceTransactions.filter { it.invoiceId == invoiceTransaction.key }.flatMap { it.transactions }
                val total = invoiceTransactions.filter { it.invoiceId == invoiceTransaction.key }.sumOf { it.total }
                val subtotal = invoiceTransactions.filter { it.invoiceId == invoiceTransaction.key }.sumOf { it.subTotal }

                outputInvoices.add(
                    GetInvoiceOutput(
                        id = invoice.id,
                        accountId = invoice.accountId,
                        status = invoice.statusType.value,
                        type = invoice.type.value,
                        subTotal = subtotal,
                        total = total,
                        mouvement = invoice.mouvementType.value,
                        date = invoice.date,
                        isFreeze = invoice.isFreeze,
                        transactions = transactions,
                        deductions = invoice.deductions.map { InvoiceDeductionOutput(
                            it.deductionId, it.amount
                        ) },
                        moduleLinkers = invoice.moduleLinkers.map { InvoiceModuleLinkerOutput(it.sourceId, it.module.value) }
                    )
                )
            }
        }

        return outputInvoices
    }
}