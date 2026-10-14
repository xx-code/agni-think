package usecases.invoices

import usecases.interfaces.IUseCase

import adapters.IAuthenticateAdapter
import domain.FREEZE_CATEGORY_ID
import adapters.repositories.IUnitOfWork
import domain.enums.InvoiceMovementType
import domain.enums.InvoiceType
import usecases.UseCase
import usecases.dto.CreatedOutput
import usecases.invoices.dto.CreateFreezeInvoiceInput
import usecases.invoices.dto.CreateInvoiceInput
import usecases.invoices.dto.TransactionInput

class CreateFreezeInvoice(
    unitOfWork: IUnitOfWork,
    private val createInvoice: IUseCase<CreateInvoiceInput, CreatedOutput>
) : UseCase<CreateFreezeInvoiceInput, CreatedOutput>(unitOfWork = unitOfWork) {

    override suspend fun process(input: CreateFreezeInvoiceInput): CreatedOutput {
        return createInvoice.processDirect(CreateInvoiceInput(
            userId = input.userId,
            accountId = input.accountId,
            status = input.status,
            date = input.endDate,
            type = InvoiceType.OTHER,
            movementType = InvoiceMovementType.DEBIT,
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