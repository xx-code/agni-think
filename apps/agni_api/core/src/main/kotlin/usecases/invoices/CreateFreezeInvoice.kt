package usecases.invoices

import domain.FREEZE_CATEGORY_ID
import adapters.repositories.IUnitOfWork
import domain.enums.InvoiceMovementType
import domain.enums.InvoiceType
import usecases.CreatedOutput
import usecases.interfaces.IInnerUseCase
import usecases.invoices.dto.CreateFreezeInvoiceInput
import usecases.invoices.dto.CreateInvoiceInput
import usecases.invoices.dto.TransactionInput

class CreateFreezeInvoice(
    val unitOfWork: IUnitOfWork,
    val createInvoice: IInnerUseCase<CreateInvoiceInput, CreatedOutput>,
) : IInnerUseCase<CreateFreezeInvoiceInput, CreatedOutput> {

    override fun execAsync(input: CreateFreezeInvoiceInput): CreatedOutput {
        return unitOfWork.execute {
            this.execInnerAsync(input)
        }
    }

    override fun execInnerAsync(input: CreateFreezeInvoiceInput): CreatedOutput {
        return createInvoice.execInnerAsync(CreateInvoiceInput(
            accountId = input.accountId,
            status = input.status,
            date = input.endDate,
            type = InvoiceType.OTHER,
            mouvementType = InvoiceMovementType.DEBIT,
            currency = null,
            isFreeze = true,
            transactions = setOf(
                TransactionInput(
                    amount = input.amount,
                    categoryId = FREEZE_CATEGORY_ID,
                    description = input.title,
                    tagIds = setOf(),
                    budgetIds = setOf()
                )
            ),
            deductions = setOf()
        ))
    }
}