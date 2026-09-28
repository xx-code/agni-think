package dev.auguste.agni_api.core.usecases.provisionable

import aQute.bnd.annotation.headers.Category
import dev.auguste.agni_api.core.adapters.dto.ScheduleRepeaterInput
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.adapters.repositories.IUnitOfWork
import dev.auguste.agni_api.core.entities.DomainException
import dev.auguste.agni_api.core.entities.Provision
import dev.auguste.agni_api.core.entities.SavingGoal
import dev.auguste.agni_api.core.entities.enums.FundType
import dev.auguste.agni_api.core.entities.enums.InvoiceType
import dev.auguste.agni_api.core.entities.enums.ProvisionType
import dev.auguste.agni_api.core.entities.enums.ScheduleInvoiceModuleLinkerType
import dev.auguste.agni_api.core.usecases.CreatedOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.provisionable.dto.CreateProvisionInput
import dev.auguste.agni_api.core.usecases.schedule_Invoices.dto.CreateScheduleInvoiceInput
import dev.auguste.agni_api.core.usecases.schedule_Invoices.dto.SchedulerInvoiceInput
import dev.auguste.agni_api.core.value_objects.ProvisionPayment
import dev.auguste.agni_api.core.value_objects.ScheduleInvoiceModuleLinker
import dev.auguste.agni_api.core.value_objects.Scheduler
import dev.auguste.agni_api.core.value_objects.SchedulerRecurrence

class CreateProvisionable(
    private val provisionRepo: IRepository<Provision>,
    private val fundRepo: IRepository<SavingGoal>,
    private val createScheduleInvoice: IUseCase<CreateScheduleInvoiceInput, CreatedOutput>,
    private val unitOfWork: IUnitOfWork,
) : IUseCase<CreateProvisionInput, CreatedOutput> {
    override fun execAsync(input: CreateProvisionInput): CreatedOutput {
        return unitOfWork.execute {
            if (provisionRepo.existsByName(input.title))
                throw DomainException.AlreadyExist.Provisionable(input.title)

            if (input.fundAmortizationId != null) {
                val fund = fundRepo.get(input.fundAmortizationId) ?: throw DomainException.NotFound.SavingGoal(input.fundAmortizationId)
                if (fund.type != FundType.AMORTIZATION)
                    throw DomainException.BusinessLogic.YouHaveToSelectOnlyAmortizationFund()
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
                    type = InvoiceType.FIXEDCOST,
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