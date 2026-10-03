package usecases.invoices

import adapters.events.contents.CreateEmbeddingInvoiceEventContent
import adapters.events.EventType
import adapters.events.IEventRegister
import adapters.repositories.IRepository
import adapters.repositories.IUnitOfWork
import domain.entities.Deduction
import domain.exceptions.NotFoundException
import domain.exceptions.ValidationException
import domain.entities.Invoice
import domain.entities.Transaction
import domain.enums.DeductionBaseType
import domain.enums.DeductionModeType
import domain.enums.InvoiceMovementType
import domain.enums.InvoiceStatusType
import facades.InvoiceDependencies
import usecases.CreatedOutput
import usecases.interfaces.IInnerUseCase
import usecases.invoices.dto.CreateInvoiceInput
import domain.value_objects.InvoiceDeduction
import java.util.UUID

// TODO: Refactoring
class CreateInvoice(
    private val invoiceRepo: IRepository<Invoice>,
    private val invoiceDependencies: InvoiceDependencies,
    private val unitOfWork: IUnitOfWork,
    private val eventRegister: IEventRegister
): IInnerUseCase<CreateInvoiceInput, CreatedOutput> {

    override fun execAsync(input: CreateInvoiceInput): CreatedOutput {
        return unitOfWork.execute {
            this.execInnerAsync(input)
        }
    }

    override fun execInnerAsync(input: CreateInvoiceInput): CreatedOutput {
        val account = invoiceDependencies.accountRepo.get(input.accountId) ?: throw NotFoundException.SingleEntity(input.accountId, "invoice")

        if (input.transactions.isEmpty())
            throw ValidationException.TransactionsMustNotBeEmpty()

        var deductions = listOf<Deduction>()
        if (input.deductions.isNotEmpty()) {
            if (input.deductions.any { it.amount < 0})
                throw ValidationException.InvoiceDeductionCannotBeNegative()

            val deductionIds = input.deductions.map { it.deductionId }.toSet()
            deductions = invoiceDependencies.deductionRepo.getManyByIds(deductionIds)
            if (input.deductions.size != deductions.size)
                throw NotFoundException.EntitiesByOtherField(mapOf("ids" to deductionIds.joinToString()), "deduction")
        }

        // TODO have sa vrification for modeleLinkers type

        val usePersistentId = input.persistentInvoiceId != null && invoiceRepo.get(input.persistentInvoiceId) == null

        val newInvoice = Invoice(
            id = if(usePersistentId) input.persistentInvoiceId else UUID.randomUUID(),
            accountId = input.accountId,
            status = input.status,
            movementType = input.mouvementType,
            type = input.type,
            deductions = input.deductions.map { InvoiceDeduction(it.deductionId, it.amount) }.toMutableSet(),
            date = input.date,
            isFreeze = input.isFreeze,
            moduleLinkers = input.moduleSourcesLinker.toMutableList()
        )

        var totalBeforeDeduction = 0.0
        input.transactions.forEach { transaction ->
            if (invoiceDependencies.categoryRepo.get(transaction.categoryId) == null)
                throw NotFoundException.SingleEntity(transaction.categoryId, "category")

            if (transaction.tagIds.isNotEmpty())
                if (transaction.tagIds.size != invoiceDependencies.tagRepo.getManyByIds(transaction.tagIds).size)
                    throw NotFoundException.EntitiesByOtherField(mapOf("ids" to transaction.tagIds.joinToString()), "tag")

            if (transaction.budgetIds.isNotEmpty())
                if (transaction.budgetIds.size != invoiceDependencies.budgetRepo.getManyByIds(transaction.budgetIds).size)
                    throw NotFoundException.EntitiesByOtherField(mapOf("ids" to transaction.budgetIds.joinToString()), "budget")

            val newTransaction = Transaction(
                invoiceId = newInvoice.id,
                categoryId = transaction.categoryId,
                tagIds = transaction.tagIds.toMutableSet(),
                budgetIds = transaction.budgetIds.toMutableSet(),
                amount = transaction.amount,
                description = transaction.description
            )

            invoiceDependencies.transactionRepo.create(newTransaction)

            totalBeforeDeduction += transaction.amount
        }

        val subTotalDeductions =  deductions.filter { it.base == DeductionBaseType.SUBTOTAL }
        val subTotal = totalBeforeDeduction + subTotalDeductions.sumOf { deduction ->
            val invoiceDeduction = input.deductions.find { it.deductionId == deduction.id }
            invoiceDeduction?.let {
                if (deduction.mode == DeductionModeType.FLAT)
                    it.amount
                else
                    totalBeforeDeduction * (it.amount / 100)
            } ?: 0.0
        }

        val totalDeductions =  deductions.filter { it.base == DeductionBaseType.TOTAL }
        val total = subTotal + totalDeductions.sumOf { deduction ->
            val invoiceDeduction = input.deductions.find { it.deductionId == deduction.id }
            invoiceDeduction?.let {
                if (deduction.mode == DeductionModeType.FLAT)
                    it.amount
                else
                    subTotal * (it.amount / 100)
            } ?: 0.0
        }

        if (input.status == InvoiceStatusType.COMPLETED) {
            if (input.mouvementType == InvoiceMovementType.CREDIT) account.balance += total
            else account.balance -= total

            invoiceDependencies.accountRepo.update(account)
        }

        invoiceRepo.create(newInvoice)
        if (newInvoice.statusType == InvoiceStatusType.COMPLETED)
            eventRegister.notify(EventType.CREATE_INVOICE, CreateEmbeddingInvoiceEventContent(newInvoice))

        return CreatedOutput(newInvoice.id)
    }
}