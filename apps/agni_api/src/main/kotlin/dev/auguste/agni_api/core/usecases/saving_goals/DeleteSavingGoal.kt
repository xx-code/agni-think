package dev.auguste.agni_api.core.usecases.saving_goals

import dev.auguste.agni_api.core.SAVING_CATEGORY_ID
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.adapters.repositories.IUnitOfWork
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryGoalExtend
import dev.auguste.agni_api.core.entities.Account
import dev.auguste.agni_api.core.entities.DomainException
import dev.auguste.agni_api.core.entities.Goal
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
    private val goalRepo: IRepository<Goal>,
    private val createInvoice: IInnerUseCase<CreateInvoiceInput, CreatedOutput>,
    private val unitOfWork: IUnitOfWork
): IUseCase<DeleteSavingGoalInput, Unit> {
    override fun execAsync(input: DeleteSavingGoalInput) {
        unitOfWork.execute {
            val savingGoal = savingGoalRepo.get(input.savingGoalId) ?: throw DomainException.NotFound.SavingGoal(input.savingGoalId)

            if (savingGoal.balance == 0.0) {
                savingGoalRepo.delete(input.savingGoalId)
                return@execute
            }

            if (input.accountId == null && savingGoal.accountId == null)
                throw DomainException.BusinessLogic.Validation("Account ID must be non-null")

            val accountId = if (savingGoal.accountId == null) {
                input.accountId!!
            } else {
                savingGoal.accountId!!
            }

            accountRepo.get(accountId) ?: throw DomainException.NotFound.Account(accountId)

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
            goalRepo.deleteManyBy(QueryGoalExtend(setOf(input.savingGoalId)))
        }
    }
}