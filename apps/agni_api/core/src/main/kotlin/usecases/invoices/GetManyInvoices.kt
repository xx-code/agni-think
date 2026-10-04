package usecases.invoices

import usecases.UseCase
import adapters.repositories.IRepository
import domain.exceptions.ValidationException
import domain.entities.Invoice
import usecases.interfaces.IUseCase
import usecases.invoices.dto.GetInvoiceOutput
import usecases.invoices.dto.InvoiceDeductionOutput
import usecases.invoices.dto.InvoiceModuleLinkerOutput
import usecases.invoices.transactions.dto.GetInvoiceTransactionsInput
import usecases.invoices.transactions.dto.GetInvoiceTransactionsOutput
import java.util.UUID

class GetManyInvoices(
    private val invoiceRepo: IRepository<Invoice>,
    private val getInvoiceTransactions: IUseCase<GetInvoiceTransactionsInput, List<GetInvoiceTransactionsOutput>>
): UseCase<Set<UUID>, List<GetInvoiceOutput>>() {
    override suspend fun process(input: Set<UUID>): List<GetInvoiceOutput> {
        val invoices = invoiceRepo.getManyByIds(input)

        val invoiceTransactions = getInvoiceTransactions.processDirect(GetInvoiceTransactionsInput(
            invoiceIds = input,
            categoryIds = null,
            tagIds = null,
            budgetIds = null,
            minAmount = null,
            maxAmount = null
        ))

        if (invoiceTransactions.isEmpty())
            throw ValidationException.TransactionsMustNotBeEmpty()

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
                        status = invoice.status.value,
                        type = invoice.type.value,
                        subTotal = subtotal,
                        total = total,
                        mouvement = invoice.movement.value,
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