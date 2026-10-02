package usecases.invoices

import adapters.repositories.IRepository
import domain.exceptions.NotFoundException
import domain.exceptions.ValidationException
import domain.entities.Invoice
import usecases.interfaces.IUseCase
import usecases.invoices.dto.GetInvoiceOutput
import usecases.invoices.dto.InvoiceDeductionOutput
import usecases.invoices.dto.InvoiceModuleLinkerOutput
import usecases.invoices.transactions.dto.GetInvoiceTransactionsInput
import usecases.invoices.transactions.dto.GetInvoiceTransactionsOutput
import java.util.UUID

class GetInvoice(
    private val invoiceRepo: IRepository<Invoice>,
    private val getInvoiceTransactions: IUseCase<GetInvoiceTransactionsInput, List<GetInvoiceTransactionsOutput>>
): IUseCase<UUID, GetInvoiceOutput> {
    override fun execAsync(input: UUID): GetInvoiceOutput {
        val invoice = invoiceRepo.get(input) ?: throw NotFoundException.SingleEntity(input, "invoice")

        val invoiceTransactions = getInvoiceTransactions.execAsync(GetInvoiceTransactionsInput(
            invoiceIds = setOf(invoice.id),
            categoryIds = null,
            tagIds = null,
            budgetIds = null,
            minAmount = null,
            maxAmount = null
        ))

        if (invoiceTransactions.isEmpty())
            throw ValidationException.TransactionsMustNotBeEmpty()


        return GetInvoiceOutput(
            id = invoice.id,
            accountId = invoice.accountId,
            status = invoice.statusType.value,
            type = invoice.type.value,
            subTotal = invoiceTransactions.first().subTotal,
            total = invoiceTransactions.first().total,
            mouvement = invoice.movementType.value,
            date = invoice.date,
            isFreeze = invoice.isFreeze,
            transactions = invoiceTransactions.first().transactions,
            deductions = invoice.deductions.map { InvoiceDeductionOutput(
                it.deductionId, it.amount
            ) },
            moduleLinkers = invoice.moduleLinkers.map { InvoiceModuleLinkerOutput(it.sourceId, it.module.value) }
        )
    }
}