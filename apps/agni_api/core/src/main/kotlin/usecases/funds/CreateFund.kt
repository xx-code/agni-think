package usecases.funds

import usecases.UseCase
import adapters.IAuthenticateAdapter
import adapters.repositories.IRepository
import adapters.repositories.IUnitOfWork
import domain.SAVING_CATEGORY_ID
import domain.entities.Account
import domain.exceptions.AlreadyExistException
import domain.exceptions.NotFoundException
import domain.exceptions.ValidationException
import domain.entities.Fund
import domain.entities.ScheduleInvoice
import domain.entities.User
import usecases.dto.CreatedOutput
import usecases.funds.dto.CreateFundInput
import domain.enums.FundType
import domain.enums.InvoiceType
import domain.enums.ScheduleInvoiceModuleLinkerType
import domain.enums.ScheduleInvoiceType
import domain.value_objects.ScheduleInvoiceModuleLinker
import domain.value_objects.Scheduler
import domain.value_objects.SinkingFundDetail

class CreateFund(
    private val fundRepo: IRepository<Fund>,
    private val accountingRepo: IRepository<Account>,
    private val scheduleInvoiceRepo: IRepository<ScheduleInvoice>,
    auth: IAuthenticateAdapter,
    uow: IUnitOfWork): UseCase<CreateFundInput, CreatedOutput>(auth = auth, unitOfWork = uow) {

    override suspend fun process(input: CreateFundInput): CreatedOutput {
        val currentUser = getCurrentUser()


        if (input.accountId != null && this.accountingRepo.get(input.accountId) == null)
            throw NotFoundException.SingleEntity(input.accountId, "account")

        if (fundRepo.existsByName(input.title))
            throw AlreadyExistException.EntitiesByField(mapOf("name" to input.title), "fund")

        if (input.type == FundType.AMORTIZATION && input.accountId != null)
            throw ValidationException.SavingGoalAmortizationMustNotHaveAccount()


        val newFund = Fund(
            userId = currentUser.id,
            title = input.title,
            description = input.description,
            accountId = input.accountId,
            target = input.target,
            type = input.type,
            balance = 0.0,
            sinkingFundDetail = input.sinkingFundDetail
        )

        fundRepo.create(newFund)

        newFund.sinkingFundDetail?.let { sinkingFundDetail ->
            var numberOfPay = sinkingFundDetail.numberOfContributions

            if (sinkingFundDetail.numberOfContributions < 1 && sinkingFundDetail.fundSchedule == null)
                throw ValidationException.SinkingFundMustHaveAnEndingDateOrNumberOfContribution()

            if(sinkingFundDetail.fundSchedule != null) {
                numberOfPay = sinkingFundDetail.contributionRecurrence.computeOccurrences(sinkingFundDetail.startDate, sinkingFundDetail.fundSchedule.date.toLocalDate())
            }

            scheduleInvoiceRepo.create(ScheduleInvoice(
                userId = currentUser.id,
                title = newFund.title + "-" + "Contribution",
                accountId = newFund.accountId!!,
                type = ScheduleInvoiceType.SAVING,
                amount = (newFund.target / numberOfPay),
                scheduler = Scheduler(
                    date = sinkingFundDetail.startDate.atStartOfDay(),
                    repeater = sinkingFundDetail.contributionRecurrence,
                ),
                categoryId = SAVING_CATEGORY_ID,
                invoiceType = InvoiceType.OTHER,
                moduleLinker = ScheduleInvoiceModuleLinker(
                    sourceId = newFund.id,
                    module = ScheduleInvoiceModuleLinkerType.FUND
                ),
                freezeScheduler = null,
                isPause = false,
                endDate = sinkingFundDetail.fundSchedule?.repeater.let {
                    sinkingFundDetail.fundSchedule?.date
                }
            ))
        }

        return CreatedOutput(newFund.id)
    }
}