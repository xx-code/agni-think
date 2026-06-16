package dev.auguste.agni_api.core.usecases.internal_loan

import dev.auguste.agni_api.core.FREEZE_CATEGORY_ID
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.adapters.repositories.IUnitOfWork
import dev.auguste.agni_api.core.entities.DomainException
import dev.auguste.agni_api.core.entities.InternalLoan
import dev.auguste.agni_api.core.entities.enums.InvoiceMouvementType
import dev.auguste.agni_api.core.entities.enums.InvoiceStatusType
import dev.auguste.agni_api.core.entities.enums.InvoiceType
import dev.auguste.agni_api.core.usecases.CreatedOutput
import dev.auguste.agni_api.core.usecases.interfaces.IInnerUseCase
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.internal_loan.dto.AddRefundInternalLoanInput
import dev.auguste.agni_api.core.usecases.invoices.dto.CreateInvoiceInput
import dev.auguste.agni_api.core.usecases.invoices.dto.GetInvoiceOutput
import dev.auguste.agni_api.core.usecases.invoices.dto.TransactionInput
import java.time.LocalDateTime
import java.util.UUID

class AddRefundInternalLoan(
    private val internalLoanRepo: IRepository<InternalLoan>,
    private val getInvoice: IUseCase<UUID, GetInvoiceOutput>,
    private val createInvoice: IInnerUseCase<CreateInvoiceInput, CreatedOutput>,
    private val unitOfWork: IUnitOfWork
): IUseCase<AddRefundInternalLoanInput, Unit> {
    override fun execAsync(input: AddRefundInternalLoanInput) {
        unitOfWork.execute {
            val internalLoan = internalLoanRepo.get(input.internalLoanId) ?: throw DomainException.NotFound.InternalLoan(input.internalLoanId)
            val invoiceLoan = getInvoice.execAsync(internalLoan.invoiceId)

            val resFreeze = createInvoice.execInnerAsync(CreateInvoiceInput(
                accountId = input.accountId,
                status = InvoiceStatusType.COMPLETED,
                date = LocalDateTime.from(internalLoan.dueDate),
                type = InvoiceType.OTHER,
                mouvementType = InvoiceMouvementType.DEBIT,
                currency = null,
                isFreeze = true,
                transactions = setOf(
                    TransactionInput(
                        amount = input.amount,
                        categoryId = FREEZE_CATEGORY_ID,
                        description = "Freeze pour internal loan ${invoiceLoan.transactions.first().description}",
                        tagIds = setOf(),
                        budgetIds = setOf()
                    )
                ),
                deductions = setOf()
            ))

            val refunds = internalLoan.trackRefunds.toMutableSet()
            refunds.add(resFreeze.newId)

            internalLoan.trackRefunds = refunds
            internalLoanRepo.update(internalLoan)
        }
    }
}