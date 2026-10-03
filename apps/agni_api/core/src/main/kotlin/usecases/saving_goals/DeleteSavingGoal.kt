package usecases.saving_goals

import domain.SAVING_CATEGORY_ID
import adapters.repositories.IRepository
import adapters.repositories.IUnitOfWork
import adapters.repositories.QueryComparator
import adapters.repositories.QueryExtendBuilder
import domain.entities.Account
import domain.exceptions.NotFoundException
import domain.exceptions.ValidationException
import domain.entities.Goal
import domain.entities.Fund
import usecases.CreatedOutput
import usecases.interfaces.IInnerUseCase
import usecases.interfaces.IUseCase
import usecases.invoices.dto.CreateInvoiceInput
import usecases.invoices.dto.TransactionInput
import usecases.saving_goals.dto.DeleteSavingGoalInput
import java.time.LocalDateTime
import domain.enums.InvoiceMovementType
import domain.enums.InvoiceStatusType
import domain.enums.InvoiceType

class DeleteSavingGoal(
    private val fundRepo: IRepository<Fund>,
    private val accountRepo: IRepository<Account>,
    private val goalRepo: IRepository<Goal>,
    private val createInvoice: IInnerUseCase<CreateInvoiceInput, CreatedOutput>,
    private val unitOfWork: IUnitOfWork
): IUseCase<DeleteSavingGoalInput, Unit> {
    override fun execAsync(input: DeleteSavingGoalInput) {
        unitOfWork.execute {
            val savingGoal = fundRepo.get(input.savingGoalId) ?: throw NotFoundException.SingleEntity(input.savingGoalId, "saving_goal")

            if (savingGoal.balance == 0.0) {
                fundRepo.delete(input.savingGoalId)
                return@execute
            }

            if (input.accountId == null && savingGoal.accountId == null)
                throw ValidationException.SavingGoalAccountIdMustNotBeNull()

            val accountId = if (savingGoal.accountId == null) {
                input.accountId!!
            } else {
                savingGoal.accountId!!
            }

            accountRepo.get(accountId) ?: throw NotFoundException.SingleEntity(accountId, "account")

            createInvoice.execInnerAsync(CreateInvoiceInput(
                accountId = accountId,
                status = InvoiceStatusType.COMPLETED,
                date = LocalDateTime.now(),
                type = InvoiceType.OTHER,
                mouvementType = InvoiceMovementType.CREDIT,
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

            fundRepo.delete(input.savingGoalId)
            val conditionGoal = QueryExtendBuilder<Goal>()
                .addCondition("targetSourceId", QueryComparator.Equal, input.savingGoalId)
            goalRepo.deleteManyBy(conditionGoal)
        }
    }
}