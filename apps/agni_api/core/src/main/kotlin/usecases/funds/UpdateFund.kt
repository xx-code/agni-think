package usecases.funds

import usecases.UseCase
import adapters.IAuthenticateAdapter
import adapters.dto.QueryFilter
import adapters.repositories.IRepository
import adapters.repositories.IUnitOfWork
import adapters.repositories.QueryComparator
import adapters.repositories.QueryExtendBuilder
import domain.SAVING_CATEGORY_ID
import domain.entities.Account
import domain.exceptions.AlreadyExistException
import domain.exceptions.NotFoundException
import domain.entities.Fund
import domain.entities.ScheduleInvoice
import usecases.funds.dto.UpdateFundInput
import domain.enums.FundType
import domain.enums.InvoiceType
import domain.enums.ScheduleInvoiceModuleLinkerType
import domain.enums.ScheduleInvoiceType
import domain.exceptions.ValidationException
import domain.value_objects.ScheduleInvoiceModuleLinker
import domain.value_objects.Scheduler

class UpdateFund(
    private val fundRepo: IRepository<Fund>,
    private val accountRepo: IRepository<Account>,
    private val scheduleInvoiceRepo: IRepository<ScheduleInvoice>,
    auth: IAuthenticateAdapter,
    uow: IUnitOfWork
): UseCase<UpdateFundInput, Unit>(auth = auth, unitOfWork = uow) {

    override suspend fun process(input: UpdateFundInput) {
        val fund = fundRepo.get(input.id) ?: throw NotFoundException.SingleEntity(input.id, "saving_goal")

        if (input.title != null) {
            if (input.title != fund.title && fundRepo.existsByName(input.title))
                throw AlreadyExistException.EntitiesByField(mapOf("name" to input.title), "saving_goal")

            fund.title = input.title
        }

        if (input.target != null)
            fund.target = input.target

        if (input.accountId != null) {
            if (accountRepo.get(input.accountId) == null)
                throw NotFoundException.SingleEntity(input.accountId, "account")

            fund.accountId = input.accountId
        }

        if (input.description != null) {
            if (input.description != fund.description) {
                fund.description = input.description
            }
        }

        if (input.sinkingFundDetail != null) {
            fund.sinkingFundDetail = input.sinkingFundDetail
        }

        if (input.type != null && input.type != fund.type ) {
            fund.type = input.type
            if (input.type == FundType.AMORTIZATION)
                fund.accountId = null

            if (fund.type != FundType.SINKING_FUND){
                fund.sinkingFundDetail = null
            }
        }

        if (fund.hasChanged()) {
            fundRepo.update(fund)

            val conditionScheduleInvoice = QueryExtendBuilder<ScheduleInvoice>()
                .addCondition("moduleLinker.sourceId", QueryComparator.Equal, fund.id)
                .addCondition("moduleLinker.module", QueryComparator.Equal, ScheduleInvoiceModuleLinkerType.FUND.value)
            val scheduleInvoices = scheduleInvoiceRepo.getAll(QueryFilter.queryAll(), conditionScheduleInvoice)

            if (fund.type != FundType.SINKING_FUND && scheduleInvoices.items.isNotEmpty()) {
                scheduleInvoiceRepo.delete(scheduleInvoices.items.first().id)
            }

            fund.sinkingFundDetail?.let { sinkingFundDetail ->
                val target = fund.target - fund.balance

                if (sinkingFundDetail.numberOfContributions < 1 && sinkingFundDetail.fundSchedule == null)
                    throw ValidationException.SinkingFundMustHaveAnEndingDateOrNumberOfContribution()

                var numberOfPay = sinkingFundDetail.numberOfContributions

                if(sinkingFundDetail.fundSchedule != null) {
                    numberOfPay = sinkingFundDetail.contributionRecurrence.computeOccurrences(sinkingFundDetail.startDate, sinkingFundDetail.fundSchedule.date.toLocalDate())
                }

                if (scheduleInvoices.items.isEmpty()) {
                    scheduleInvoiceRepo.create(ScheduleInvoice(
                        userId = fund.userId,
                        title = fund.title + "-" + "Contribution",
                        accountId = fund.accountId!!,
                        type = ScheduleInvoiceType.SAVING,
                        amount = (target / numberOfPay),
                        scheduler = Scheduler(
                            date = sinkingFundDetail.startDate.atStartOfDay(),
                            repeater = sinkingFundDetail.contributionRecurrence
                        ),
                        categoryId = SAVING_CATEGORY_ID,
                        invoiceType = InvoiceType.OTHER,
                        moduleLinker = ScheduleInvoiceModuleLinker(
                            sourceId = fund.id,
                            module = ScheduleInvoiceModuleLinkerType.FUND
                        ),
                        freezeScheduler = null,
                        isPause = false,
                        endDate = sinkingFundDetail.fundSchedule?.repeater.let {
                            sinkingFundDetail.fundSchedule?.date
                        }
                    ))
                } else {
                    val existSchedule = scheduleInvoices.items.first()

                    var startDate = existSchedule.scheduler.date
                    if (startDate < sinkingFundDetail.startDate.atStartOfDay())
                        startDate = sinkingFundDetail.startDate.atStartOfDay()

                    existSchedule.title = fund.title + "-" + "Contribution"
                    existSchedule.accountId = fund.accountId!!
                    existSchedule.amount = (target / numberOfPay)
                    existSchedule.endDate = sinkingFundDetail.fundSchedule?.repeater.let {
                        sinkingFundDetail.fundSchedule?.date
                    }
                    existSchedule.scheduler = Scheduler(
                        date = startDate,
                        repeater = sinkingFundDetail.contributionRecurrence
                    )

                    scheduleInvoiceRepo.update(existSchedule)
                }
            }
        }
    }
}