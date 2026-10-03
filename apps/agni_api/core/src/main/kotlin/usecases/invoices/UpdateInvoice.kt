package usecases.invoices

import usecases.interfaces.IUseCase

import adapters.dto.QueryFilter
import adapters.repositories.IRepository
import adapters.repositories.IUnitOfWork
import adapters.repositories.QueryExtendBuilder
import adapters.repositories.QueryComparator
import domain.entities.InternalLoan
import domain.entities.Invoice
import facades.InvoiceDependencies
import usecases.dto.CreatedOutput
import usecases.invoices.dto.CreateInvoiceInput
import usecases.invoices.dto.DeleteInvoiceInput
import usecases.invoices.dto.InvoiceDeductionInput
import usecases.invoices.dto.TransactionInput
import usecases.invoices.dto.UpdateInvoiceInput
import usecases.invoices.transactions.dto.GetInvoiceTransactionsInput
import usecases.invoices.transactions.dto.GetInvoiceTransactionsOutput
import domain.value_objects.InvoiceDeduction
import domain.exceptions.NotFoundException
import domain.exceptions.ValidationException
import usecases.UseCase

class UpdateInvoice(
    private val invoiceRepo: IRepository<Invoice>,
    private val invoiceDependencies: InvoiceDependencies,
    private val createInvoice: IUseCase<CreateInvoiceInput, CreatedOutput>,
    private val deleteInvoice: IUseCase<DeleteInvoiceInput, Unit>,
    private val getInvoiceTransactions: IUseCase<GetInvoiceTransactionsInput, List<GetInvoiceTransactionsOutput>>,
    unitOfWork: IUnitOfWork
): UseCase<UpdateInvoiceInput, Unit>(unitOfWork) {
    override suspend fun process(
        input: UpdateInvoiceInput
    ) {
        val invoice = invoiceRepo.get(input.id) ?: throw NotFoundException.SingleEntity(input.id, "invoice")

        val conditionInternalLoan = QueryExtendBuilder<InternalLoan>()
            .addCondition("invoiceId", QueryComparator.Equal, input.id)
        val internalLoans = invoiceDependencies.internalLoanRepo.getAll(QueryFilter(queryAll = true), conditionInternalLoan)
        if (internalLoans.items.isNotEmpty()) {
            throw ValidationException.InternalLoanLinkCantBeDelete()
        }

        val conditionInternalLoanReturn = QueryExtendBuilder<InternalLoan>()
            .addCondition("trackRefunds", QueryComparator.In, invoice.id)
        val internalLoanRefunds = invoiceDependencies.internalLoanRepo.getAll(QueryFilter(queryAll = true), conditionInternalLoanReturn)
        if (internalLoanRefunds.items.isNotEmpty()) {
            throw ValidationException.InternalLoanLinkCantBeDelete()
        }

        if (input.accountId != null) {
            if (invoiceDependencies.accountRepo.get(input.accountId) == null)
                throw NotFoundException.SingleEntity(input.accountId, "account")

            invoice.accountId = input.accountId
        }

        if (input.type != null)
            invoice.type = input.type

        if (input.mouvementType != null)
            invoice.movementType = input.mouvementType

        if (input.date != null)
            invoice.date = input.date

        if (input.deductions != null)
            invoice.deductions = input.deductions.map { InvoiceDeduction(it.deductionId, it.amount) }.toMutableSet()

        val anyTransactionChange = input.addTransactions.isNotEmpty() || input.removeTransactionIds.isNotEmpty()

        if (invoice.hasChanged() || anyTransactionChange) {
            // 1. Récupérer les transactions existantes
            val existingTransactionsOutput = getInvoiceTransactions.processDirect(
                GetInvoiceTransactionsInput(
                    invoiceIds = setOf(invoice.id),
                    categoryIds = null,
                    tagIds = null,
                    budgetIds = null,
                    minAmount = null,
                    maxAmount = null
                )
            ).firstOrNull()?.transactions ?: emptyList()

            // 2. Filtrer les transactions supprimées et les convertir en TransactionInput
            val remainingTransactions = existingTransactionsOutput
                .filterNot { it.id in input.removeTransactionIds }
                .map { it ->
                    TransactionInput(
                        amount = it.amount,
                        categoryId = it.category.id,
                        description = it.description,
                        tagIds = it.tags.map { tag -> tag.id }.toSet(),
                        budgetIds = it.budgets.map { budget -> budget.id }.toSet(),
                    )
                }

            // 3. Fusionner les transactions conservées avec les nouvelles
            val finalTransactions = (remainingTransactions + input.addTransactions).toSet()

            deleteInvoice.processDirect(DeleteInvoiceInput(invoice.id))

            createInvoice.processDirect(CreateInvoiceInput(
                persistentInvoiceId = invoice.id,
                accountId = invoice.accountId,
                status = invoice.statusType,
                date = invoice.date,
                type = invoice.type,
                mouvementType = invoice.movementType,
                currency = null,
                isFreeze = invoice.isFreeze,
                transactions = finalTransactions,
                deductions = invoice.deductions.map { InvoiceDeductionInput(
                    it.deductionId, it.amount
                ) }.toSet()
            ))

        }
    }
}