package usecases.funds

import usecases.interfaces.IUseCase
import adapters.IAuthenticateAdapter
import adapters.dto.QueryFilter

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
import domain.entities.ScheduleInvoice
import usecases.dto.CreatedOutput
import usecases.invoices.dto.CreateInvoiceInput
import usecases.invoices.dto.TransactionInput
import usecases.funds.dto.DeleteFundInput
import java.time.LocalDateTime
import domain.enums.InvoiceMovementType
import domain.enums.InvoiceStatusType
import domain.enums.InvoiceType
import domain.enums.ScheduleInvoiceModuleLinkerType
import usecases.UseCase
import usecases.schedule_Invoices.dto.DeleteScheduleInvoiceInput

class DeleteFund(
    private val fundRepo: IRepository<Fund>,
    private val accountRepo: IRepository<Account>,
    private val goalRepo: IRepository<Goal>,
    private val createInvoice: IUseCase<CreateInvoiceInput, CreatedOutput>,
    private val scheduleInvoiceRepo: IRepository<ScheduleInvoice>,
    private val deleteScheduleInvoice: IUseCase<DeleteScheduleInvoiceInput, Unit>,
    unitOfWork: IUnitOfWork,
    auth: IAuthenticateAdapter
): UseCase<DeleteFundInput, Unit>(unitOfWork = unitOfWork, auth = auth) {
    override suspend fun process(input: DeleteFundInput) {
        val fund = fundRepo.get(input.fundId) ?: throw NotFoundException.SingleEntity(input.fundId, "saving_goal")

        if (fund.balance == 0.0) {
            fundRepo.delete(input.fundId)
            return
        }

        if (input.accountId == null && fund.accountId == null)
            throw ValidationException.SavingGoalAccountIdMustNotBeNull()

        val accountId = if (fund.accountId == null) {
            input.accountId!!
        } else {
            fund.accountId!!
        }

        accountRepo.get(accountId) ?: throw NotFoundException.SingleEntity(accountId, "account")

        createInvoice.processDirect(CreateInvoiceInput(
            userId = fund.userId,
            accountId = accountId,
            status = InvoiceStatusType.COMPLETED,
            date = LocalDateTime.now(),
            type = InvoiceType.OTHER,
            movementType = InvoiceMovementType.CREDIT,
            currency = null,
            transactions = setOf(TransactionInput(
                amount = fund.balance,
                categoryId = SAVING_CATEGORY_ID,
                description = "Argent plan d'epargne ${fund.title}",
                tagIds = setOf(),
                budgetIds = setOf()
            )),
            deductions = setOf()
        ))

        fundRepo.delete(input.fundId)

        // Delete goal
        val conditionGoal = QueryExtendBuilder<Goal>()
            .addCondition("targetSourceId", QueryComparator.Equal, input.fundId)
        goalRepo.deleteManyBy(conditionGoal)

        // Delete schedule Saving
        val scheduleInvoiceCondition = QueryExtendBuilder<ScheduleInvoice>()
            .addCondition("moduleLinker.sourceId", QueryComparator.Equal, fund.id)
            .addCondition("moduleLinker.module", QueryComparator.Equal, ScheduleInvoiceModuleLinkerType.FUND.value)
        val scheduleInvoices = scheduleInvoiceRepo.getAll(QueryFilter.queryAll(), scheduleInvoiceCondition)
        for (scheduleInvoice in scheduleInvoices.items) {
            deleteScheduleInvoice.processDirect(DeleteScheduleInvoiceInput(
                scheduleInvoiceId = scheduleInvoice.id,
                passContextEdit = true
            ))
        }
    }
}