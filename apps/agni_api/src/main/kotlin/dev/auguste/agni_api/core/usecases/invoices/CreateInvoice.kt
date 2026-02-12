package dev.auguste.agni_api.core.usecases.invoices

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.adapters.repositories.IUnitOfWork
import dev.auguste.agni_api.core.entities.Deduction
import dev.auguste.agni_api.core.entities.Invoice
import dev.auguste.agni_api.core.entities.Transaction
import dev.auguste.agni_api.core.entities.enums.DeductionBaseType
import dev.auguste.agni_api.core.entities.enums.DeductionModeType
import dev.auguste.agni_api.core.entities.enums.InvoiceMouvementType
import dev.auguste.agni_api.core.entities.enums.InvoiceStatusType
import dev.auguste.agni_api.core.facades.InvoiceDependencies
import dev.auguste.agni_api.core.usecases.CreatedOutput
import dev.auguste.agni_api.core.usecases.interfaces.IInnerUseCase
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.invoices.dto.CreateInvoiceInput
import dev.auguste.agni_api.core.value_objects.InvoiceDeduction

class CreateInvoice(
    private val invoiceRepo: IRepository<Invoice>,
    private val invoiceDependencies: InvoiceDependencies,
    private val unitOfWork: IUnitOfWork
): IInnerUseCase<CreateInvoiceInput, CreatedOutput> {

    override fun execAsync(input: CreateInvoiceInput): CreatedOutput {
        return unitOfWork.execute {
            this.execInnerAsync(input)
        }
    }

    override fun execInnerAsync(input: CreateInvoiceInput): CreatedOutput {
        val account = invoiceDependencies.accountRepo.get(input.accountId) ?: throw Error("Account ID ${input.accountId} not found")

        if (input.transactions.isEmpty())
            throw Error("Transactions cannot be empty")

        var deductions = listOf<Deduction>()
        if (input.deductions.isNotEmpty()) {
            if (input.deductions.any { it.amount < 0})
                throw Error("Deduction cannot be negative")

            deductions = invoiceDependencies.deductionRepo.getManyByIds(input.deductions.map { it.deductionId}.toSet())
            if (input.deductions.size != deductions.size)
                throw Error("Deductions cannot be empty")
        }


        val newInvoice = Invoice(
            accountId = input.accountId,
            status = input.status,
            mouvementType = input.mouvementType,
            type = input.type,
            deductions = input.deductions.map { InvoiceDeduction(it.deductionId, it.amount) }.toMutableSet(),
            date = input.date,
            isFreeze = input.isFreeze
        )

        var totalBeforeDeduction = 0.0
        input.transactions.forEach { transaction ->
            if (invoiceDependencies.categoryRepo.get(transaction.categoryId) == null)
                throw Error("Category ${transaction.categoryId} not found")

            if (transaction.tagIds.isNotEmpty())
                if (transaction.tagIds.size != invoiceDependencies.tagRepo.getManyByIds(transaction.tagIds).size)
                    throw Error("Tags not found")

            if (transaction.budgetIds.isNotEmpty())
                if (transaction.budgetIds.size != invoiceDependencies.budgetRepo.getManyByIds(transaction.budgetIds).size)
                    throw Error("Budget not found")

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
            }
            0
        }

        val totalDeductions =  deductions.filter { it.base == DeductionBaseType.TOTAL }
        val total = subTotal + totalDeductions.sumOf { deduction ->
            val invoiceDeduction = input.deductions.find { it.deductionId == deduction.id }
            invoiceDeduction?.let {
                if (deduction.mode == DeductionModeType.FLAT)
                    it.amount
                else
                    subTotal * (it.amount / 100)
            }
            0
        }

        if (input.status == InvoiceStatusType.COMPLETED) {
            if (input.mouvementType == InvoiceMouvementType.CREDIT) account.balance += total
            else account.balance -= total

            invoiceDependencies.accountRepo.update(account)
        }

        invoiceRepo.create(newInvoice)

        return CreatedOutput(newInvoice.id)
    }
}