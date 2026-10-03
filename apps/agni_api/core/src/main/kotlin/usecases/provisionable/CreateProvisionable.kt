package usecases.provisionable

import adapters.dto.ScheduleRepeaterInput
import adapters.repositories.IRepository
import adapters.repositories.IUnitOfWork
import domain.exceptions.AlreadyExistException
import domain.exceptions.NotFoundException
import domain.exceptions.ValidationException
import domain.entities.Provision
import domain.entities.Fund
import usecases.CreatedOutput
import usecases.interfaces.IUseCase
import usecases.provisionable.dto.CreateProvisionInput
import usecases.schedule_Invoices.dto.CreateScheduleInvoiceInput
import usecases.schedule_Invoices.dto.SchedulerInvoiceInput
import domain.value_objects.ProvisionPayment
import domain.value_objects.ScheduleInvoiceModuleLinker
import domain.value_objects.Scheduler
import domain.value_objects.SchedulerRecurrence
import domain.enums.FundType
import domain.enums.InvoiceType
import domain.enums.ProvisionType
import domain.enums.ScheduleInvoiceModuleLinkerType

class CreateProvisionable(
    private val provisionRepo: IRepository<Provision>,
    private val fundRepo: IRepository<Fund>,
    private val createScheduleInvoice: IUseCase<CreateScheduleInvoiceInput, CreatedOutput>,
    private val unitOfWork: IUnitOfWork,
) : IUseCase<CreateProvisionInput, CreatedOutput> {
    override fun execAsync(input: CreateProvisionInput): CreatedOutput {
        return unitOfWork.execute {
            if (provisionRepo.existsByName(input.title))
                throw AlreadyExistException.EntitiesByField(mapOf("name" to input.title), "provisionable")

            if (input.fundAmortizationId != null) {
                val fund = fundRepo.get(input.fundAmortizationId) ?: throw NotFoundException.SingleEntity(input.fundAmortizationId, "saving_goal")
                if (fund.type != FundType.AMORTIZATION)
                    throw ValidationException.YouHaveToSelectOnlyAmortizationFund()
            }

            val provision = Provision(
                title = input.title,
                costHT = input.costHT,
                costTTC = input.costTTC,
                acquisitionDate = input.acquisitionDate,
                expectedLifespanMonth = input.expectedLifespanMonth,
                isPatrimony = input.isPatrimony,
                fundAmortizationId = input.fundAmortizationId,
                depreciationCriteria = input.depreciationCriteria.toMutableList(),
                floorValue = input.floorValue,
                type = input.type,
                interestLoan = input.interestLoan,
                loanMonth = input.loanMonth.toLong(),
            )

            if (input.scheduleInvoice != null && input.type == ProvisionType.DEPRECIATE_LOAN) {
                val endLoanDate = provision.acquisitionDate.plusMonths(input.loanMonth.toLong())
                val scheduler = Scheduler(
                    date = input.acquisitionDate.atStartOfDay(),
                    repeater = SchedulerRecurrence(
                        period = input.scheduleInvoice.paymentPeriod,
                        interval = input.scheduleInvoice.paymentInterval
                    )
                )
                scheduler.date = scheduler.upgradeDate()

                val loanAmount = ProvisionCommon.determineScheduleInvoiceDepreciateLoan(
                    initialCost = provision.calculateTotalCost(),
                    monthlyPayment = provision.calculateMonthlyPayment(),
                    scheduler = scheduler
                )

                val payment = ProvisionPayment(
                    accountId = input.scheduleInvoice.invoiceAccountId,
                    categoryId = input.scheduleInvoice.invoiceCategoryId,
                    budgetIds = input.scheduleInvoice.budgetIds,
                    tagIds = input.scheduleInvoice.tagIds,
                    paymentAmount = loanAmount,
                    scheduler = scheduler,
                    endDate = endLoanDate
                )

                provision.paymentInfo = payment
            }

            provisionRepo.create(provision)
            if (provision.type == ProvisionType.DEPRECIATE_LOAN && provision.paymentInfo != null) {
                createScheduleInvoice.execAsync(CreateScheduleInvoiceInput(
                    accountId = provision.paymentInfo!!.accountId,
                    amount = provision.paymentInfo!!.paymentAmount,
                    description = provision.title,
                    categoryId = provision.paymentInfo!!.categoryId,
                    tagIds = provision.paymentInfo!!.tagIds,
                    type = InvoiceType.FIXED_COST,
                    schedule = SchedulerInvoiceInput(
                        dueDate = provision.paymentInfo!!.scheduler.date,
                        repeater = provision.paymentInfo!!.scheduler.repeater?.let {
                            ScheduleRepeaterInput(
                                period = it.period,
                                interval = it.interval,
                            )
                        }
                    ),
                    isFreeze = false,
                    freezeSchedule = null,
                    moduleLinker = ScheduleInvoiceModuleLinker(
                        provision.id,
                        ScheduleInvoiceModuleLinkerType.PROVISION,
                    ),
                    endDate = provision.paymentInfo!!.endDate.atStartOfDay()
                ))
            }

            CreatedOutput(provision.id)
        }
    }
}