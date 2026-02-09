package dev.auguste.agni_api.core.usecases.saving_goals

import dev.auguste.agni_api.core.SAVING_CATEGORY_ID
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.adapters.repositories.IUnitOfWork
import dev.auguste.agni_api.core.entities.Account
import dev.auguste.agni_api.core.entities.SavingGoal
import dev.auguste.agni_api.core.entities.enums.InvoiceMouvementType
import dev.auguste.agni_api.core.entities.enums.InvoiceStatusType
import dev.auguste.agni_api.core.entities.enums.InvoiceType
import dev.auguste.agni_api.core.usecases.CreatedOutput
import dev.auguste.agni_api.core.usecases.interfaces.IInnerUseCase
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.invoices.dto.CreateInvoiceInput
import dev.auguste.agni_api.core.usecases.invoices.dto.TransactionInput
import dev.auguste.agni_api.core.usecases.saving_goals.dto.DeleteSavingGoalInput
import java.time.LocalDateTime

class DeleteSavingGoal(
    private val savingGoalRepo: IRepository<SavingGoal>,
    private val accountRepo: IRepository<Account>,
    private val createInvoice: IInnerUseCase<CreateInvoiceInput, CreatedOutput>,
    private val unitOfWork: IUnitOfWork
): IUseCase<DeleteSavingGoalInput, Unit> {
    override fun execAsync(input: DeleteSavingGoalInput) {
        unitOfWork.execute {
            val savingGoal = savingGoalRepo.get(input.savingGoalId) ?: throw Error("Could not find saving goal")

            val accountId = if (savingGoal.accountId == null) {
                if (input.accountId == null)
                    throw Error("Account ID must be non-null.")
                input.accountId
            } else {
                if (savingGoal.accountId == null)
                    throw Error("Account ID Link to saving is null.")
                savingGoal.accountId!!
            }

            accountRepo.get(accountId) ?: throw Error("Account not found.")

            createInvoice.execInnerAsync(CreateInvoiceInput(
                accountId = accountId,
                status = InvoiceStatusType.COMPLETED,
                date = LocalDateTime.now(),
                type = InvoiceType.OTHER,
                mouvementType = InvoiceMouvementType.CREDIT,
                currency = null,
                transactions = setOf(TransactionInput(
                    amount = savingGoal.balance,
                    categoryId = SAVING_CATEGORY_ID,
                    description = "Argent plan d'epargne ${savingGoal.title}",
                    tagIds = setOf(),
                    budgetIds = setOf()
                )),
                deductions = setOf()
            ))

            savingGoalRepo.delete(input.savingGoalId)
        }
    }
}