package dev.auguste.agni_api.core.usecases.invoices

import dev.auguste.agni_api.core.FREEZE_CATEGORY_ID
import dev.auguste.agni_api.core.entities.enums.InvoiceMouvementType
import dev.auguste.agni_api.core.entities.enums.InvoiceType
import dev.auguste.agni_api.core.usecases.CreatedOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.invoices.dto.CreateFreezeInvoiceInput
import dev.auguste.agni_api.core.usecases.invoices.dto.CreateInvoiceInput
import dev.auguste.agni_api.core.usecases.invoices.dto.TransactionInput

class CreateFreezeInvoice(
    val createInvoice: IUseCase<CreateInvoiceInput, CreatedOutput>
) : IUseCase<CreateFreezeInvoiceInput, CreatedOutput> {

    override fun execAsync(input: CreateFreezeInvoiceInput): CreatedOutput {
        return createInvoice.execAsync(CreateInvoiceInput(
            accountId = input.accountId,
            status = input.status,
            date = input.endDate,
            type = InvoiceType.OTHER,
            mouvementType = InvoiceMouvementType.DEBIT,
            currency = null,
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