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
import usecases.invoices.dto.CreateInvoiceInput
import usecases.invoices.dto.TransactionInput
import usecases.saving_goals.dto.DecreaseSavingGoalInput
import domain.value_objects.InvoiceModuleLinker
import java.time.LocalDateTime

class DecreaseSavingGoal(
    private val fundRepo: IRepository<Fund>,
    private val accountRepo: IRepository<Account>,
    private val createInvoice: IInnerUseCase<CreateInvoiceInput, CreatedOutput>,
    private val unitOfWork: IUnitOfWork
): IInnerUseCase<DecreaseSavingGoalInput, Unit> {

    override fun execAsync(input: DecreaseSavingGoalInput) {
        unitOfWork.execute {
            execInnerAsync(input)
        }
    }

    override fun execInnerAsync(input: DecreaseSavingGoalInput) {
        val savingGoal = fundRepo.get(input.savingGoalId) ?: throw NotFoundException.SingleEntity(input.savingGoalId, "saving_goal")

        if (input.amount <= 0)
            throw ValidationException.SavingGoalAmountMustBeGreaterThanZero()

        if (savingGoal.balance < input.amount)
            throw ValidationException.SavingGoalBalanceMustBeGreaterThanAmount()

        if (savingGoal.accountId != null && input.accountId != savingGoal.accountId)
            throw ValidationException.SavingGoalAccountIdMustNotMatch()


        val account = accountRepo.get(input.accountId) ?: throw NotFoundException.SingleEntity(input.accountId, "account")
        if (account.balance < input.amount)
            throw ValidationException.SavingGoalBalanceMustBeGreaterThanAmount()

        createInvoice.execInnerAsync(CreateInvoiceInput(
            accountId = input.accountId,
            status = _root_ide_package_.domain.enums.InvoiceStatusType.COMPLETED,
            date = LocalDateTime.now(),
            type = _root_ide_package_.domain.enums.InvoiceType.OTHER,
            mouvementType = _root_ide_package_.domain.enums.InvoiceMovementType.CREDIT,
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
                module = _root_ide_package_.domain.enums.InvoiceModuleLinkerType.FUND
            )),
            deductions = setOf()
        ))

        savingGoal.balance -= input.amount
        fundRepo.update(savingGoal)
    }
}