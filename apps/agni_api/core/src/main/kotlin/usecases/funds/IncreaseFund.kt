package usecases.funds

import usecases.interfaces.IUseCase
import adapters.IAuthenticateAdapter

import domain.SAVING_CATEGORY_ID
import adapters.repositories.IRepository
import adapters.repositories.IUnitOfWork
import domain.entities.Account
import domain.exceptions.NotFoundException
import domain.exceptions.ValidationException
import domain.entities.Fund
import domain.entities.User
import usecases.dto.CreatedOutput
import usecases.invoices.dto.CreateInvoiceInput
import usecases.invoices.dto.TransactionInput
import domain.value_objects.InvoiceModuleLinker
import java.time.LocalDateTime
import domain.enums.InvoiceModuleLinkerType
import domain.enums.InvoiceMovementType
import domain.enums.InvoiceStatusType
import domain.enums.InvoiceType
import usecases.UseCase
import usecases.funds.dto.IncreaseFundInput

class IncreaseFund(
    private val fundRepo: IRepository<Fund>,
    private val accountRepo: IRepository<Account>,
    private val createInvoice: IUseCase<CreateInvoiceInput, CreatedOutput>,
    private val updateFundScheduleInvoice: IUseCase<Fund, Unit>,
    private val userRepo: IRepository<User>,
    unitOfWork: IUnitOfWork
): UseCase<IncreaseFundInput, Unit>(unitOfWork = unitOfWork) {
    override suspend fun process(input: IncreaseFundInput) {
        userRepo.get(input.userId) ?: throw NotFoundException.SingleEntity(input.userId, "user")

        val fund = fundRepo.get(input.fundId) ?: throw NotFoundException.SingleEntity(input.fundId, "fund")

        if (input.amount <= 0)
            throw ValidationException.SavingGoalAmountMustBeGreaterThanZero()

        if (fund.accountId != null && input.accountId != fund.accountId)
            throw ValidationException.SavingGoalAccountIdMustNotMatch()

        val account = accountRepo.get(input.accountId) ?: throw NotFoundException.SingleEntity(input.accountId, "account")

        if (input.amount > account.balance)
            throw ValidationException.SavingGoalBalanceMustBeLesserThanAmount()

        if (input.amount > (fund.target - fund.balance))
            throw ValidationException.FundIncreaseMustBeLesserOrEqualToFundAmount(input.amount)

        createInvoice.processDirect(CreateInvoiceInput(
            accountId = input.accountId,
            userId = account.userId,
            status = InvoiceStatusType.COMPLETED,
            date = LocalDateTime.now(),
            type = InvoiceType.OTHER,
            movementType = InvoiceMovementType.DEBIT,
            currency = null,
            transactions = setOf(TransactionInput(
                amount = input.amount,
                categoryId = SAVING_CATEGORY_ID,
                description = "Argent plan d'epargne ${fund.title}",
                tagIds = setOf(),
                budgetIds = setOf()
            )),
            moduleSourcesLinker = listOf(InvoiceModuleLinker(
                sourceId = fund.id,
                module = InvoiceModuleLinkerType.FUND
            )),
            deductions = setOf()
        ))

        fund.balance += input.amount
        fundRepo.update(fund)

        updateFundScheduleInvoice.processDirect(fund)
    }
}