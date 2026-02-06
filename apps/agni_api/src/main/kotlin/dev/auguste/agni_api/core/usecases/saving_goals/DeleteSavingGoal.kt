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
import dev.auguste.agni_api.core.usecases.saving_goals.dto.DeleteSavingGaolInput
import java.time.LocalDateTime
import java.util.Date

class DeleteSavingGoal(
    val savingGoalRepo: IRepository<SavingGoal>,
    val accountRepo: IRepository<Account>,
    val createInvoice: IInnerUseCase<CreateInvoiceInput, CreatedOutput>,
    val unitOfWork: IUnitOfWork
): IUseCase<DeleteSavingGaolInput, Unit> {
    override fun execAsync(input: DeleteSavingGaolInput) {
        try {
            unitOfWork.start()

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

            unitOfWork.commit()
        } catch (error: Throwable) {
            unitOfWork.rollback()
            throw error
        }
    }
}