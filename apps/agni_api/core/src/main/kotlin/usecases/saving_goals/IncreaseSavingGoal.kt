package usecases.saving_goals

import domain.SAVING_CATEGORY_ID
import adapters.repositories.IRepository
import adapters.repositories.IUnitOfWork
import domain.entities.Account
import domain.exceptions.NotFoundException
import domain.exceptions.ValidationException
import domain.entities.Fund
import usecases.CreatedOutput
import usecases.interfaces.IInnerUseCase
import usecases.interfaces.IUseCase
import usecases.invoices.dto.CreateInvoiceInput
import usecases.invoices.dto.TransactionInput
import usecases.saving_goals.dto.IncreaseSavingGoalInput
import domain.value_objects.InvoiceModuleLinker
import java.time.LocalDateTime
import domain.enums.InvoiceModuleLinkerType
import domain.enums.InvoiceMovementType
import domain.enums.InvoiceStatusType
import domain.enums.InvoiceType

class IncreaseSavingGoal(
    private val fundRepo: IRepository<Fund>,
    private val accountRepo: IRepository<Account>,
    private val createInvoice: IInnerUseCase<CreateInvoiceInput, CreatedOutput>,
    private val unitOfWork: IUnitOfWork
): IUseCase<IncreaseSavingGoalInput, Unit> {
    override fun execAsync(input: IncreaseSavingGoalInput) {
        unitOfWork.execute {
            val savingGoal = fundRepo.get(input.savingGoalId) ?: throw NotFoundException.SingleEntity(input.savingGoalId, "saving_goal")

            if (input.amount <= 0)
                throw ValidationException.SavingGoalAmountMustBeGreaterThanZero()

            if (savingGoal.accountId != null && input.accountId != savingGoal.accountId)
                throw ValidationException.SavingGoalAccountIdMustNotMatch()

            val account = accountRepo.get(input.accountId) ?: throw NotFoundException.SingleEntity(input.accountId, "account")

            if (input.amount > account.balance)
                throw ValidationException.SavingGoalBalanceMustBeLesserThanAmount()

            createInvoice.execInnerAsync(CreateInvoiceInput(
                accountId = input.accountId,
                status = InvoiceStatusType.COMPLETED,
                date = LocalDateTime.now(),
                type = InvoiceType.OTHER,
                mouvementType = InvoiceMovementType.DEBIT,
                currency = null,
                transactions = setOf(TransactionInput(
                    amount = input.amount,
                    categoryId = SAVING_CATEGORY_ID,
                    description = "Argent plan d'epargne ${savingGoal.title}",
                    tagIds = setOf(),
                    budgetIds = setOf()
                )),
                moduleSourcesLinker = listOf(InvoiceModuleLinker(
                    sourceId = savingGoal.id,
                    module = InvoiceModuleLinkerType.FUND
                )),
                deductions = setOf()
            ))

            savingGoal.balance += input.amount
            fundRepo.update(savingGoal)
        }
    }
}