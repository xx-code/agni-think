package usecases.funds

import usecases.interfaces.IUseCase

import domain.SAVING_CATEGORY_ID
import adapters.repositories.IRepository
import adapters.repositories.IUnitOfWork
import domain.entities.Account
import domain.exceptions.NotFoundException
import domain.exceptions.ValidationException
import domain.entities.Fund
import usecases.dto.CreatedOutput
import usecases.invoices.dto.CreateInvoiceInput
import usecases.invoices.dto.TransactionInput
import usecases.funds.dto.DecreaseSavingGoalInput
import domain.value_objects.InvoiceModuleLinker
import java.time.LocalDateTime
import domain.enums.InvoiceModuleLinkerType
import domain.enums.InvoiceMovementType
import domain.enums.InvoiceStatusType
import domain.enums.InvoiceType
import usecases.UseCase

class DecreaseFund(
    private val fundRepo: IRepository<Fund>,
    private val accountRepo: IRepository<Account>,
    private val createInvoice: IUseCase<CreateInvoiceInput, CreatedOutput>,
    unitOfWork: IUnitOfWork
): UseCase<DecreaseSavingGoalInput, Unit>(unitOfWork) {

    override suspend fun process(input: DecreaseSavingGoalInput) {
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

        createInvoice.processDirect(CreateInvoiceInput(
            accountId = input.accountId,
            status = InvoiceStatusType.COMPLETED,
            date = LocalDateTime.now(),
            type = InvoiceType.OTHER,
            mouvementType = InvoiceMovementType.CREDIT,
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

        savingGoal.balance -= input.amount
        fundRepo.update(savingGoal)
    }
}