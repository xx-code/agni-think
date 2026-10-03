package usecases.saving_goals

import usecases.interfaces.IUseCase

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
import usecases.dto.CreatedOutput
import usecases.invoices.dto.CreateInvoiceInput
import usecases.invoices.dto.TransactionInput
import usecases.saving_goals.dto.DeleteSavingGoalInput
import java.time.LocalDateTime
import domain.enums.InvoiceMovementType
import domain.enums.InvoiceStatusType
import domain.enums.InvoiceType
import usecases.UseCase

class DeleteSavingGoal(
    private val fundRepo: IRepository<Fund>,
    private val accountRepo: IRepository<Account>,
    private val goalRepo: IRepository<Goal>,
    private val createInvoice: IUseCase<CreateInvoiceInput, CreatedOutput>,
    unitOfWork: IUnitOfWork
): UseCase<DeleteSavingGoalInput, Unit>(unitOfWork) {
    override suspend fun process(input: DeleteSavingGoalInput) {
        val savingGoal = fundRepo.get(input.savingGoalId) ?: throw NotFoundException.SingleEntity(input.savingGoalId, "saving_goal")

        if (savingGoal.balance == 0.0) {
            fundRepo.delete(input.savingGoalId)
            return
        }

        if (input.accountId == null && savingGoal.accountId == null)
            throw ValidationException.SavingGoalAccountIdMustNotBeNull()

        val accountId = if (savingGoal.accountId == null) {
            input.accountId!!
        } else {
            savingGoal.accountId!!
        }

        accountRepo.get(accountId) ?: throw NotFoundException.SingleEntity(accountId, "account")

        createInvoice.processDirect(CreateInvoiceInput(
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