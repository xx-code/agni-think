package usecases.provisionable

import usecases.interfaces.IUseCase

import adapters.dto.QueryFilter
import adapters.dto.ScheduleRepeaterInput
import adapters.repositories.IRepository
import adapters.repositories.IUnitOfWork
import adapters.repositories.QueryExtendBuilder
import adapters.repositories.QueryComparator
import domain.exceptions.AlreadyExistException
import domain.exceptions.NotFoundException
import domain.exceptions.UnExpectedException
import domain.exceptions.ValidationException
import domain.entities.Provision
import domain.entities.Fund
import domain.entities.ScheduleInvoice
import usecases.provisionable.dto.UpdateProvisionInput
import usecases.schedule_Invoices.dto.SchedulerInvoiceInput
import usecases.schedule_Invoices.dto.UpdateScheduleInvoiceInput
import domain.value_objects.ProvisionPayment
import domain.value_objects.Scheduler
import domain.value_objects.SchedulerRecurrence
import domain.enums.FundType
import domain.enums.ProvisionType
import domain.enums.ScheduleInvoiceModuleLinkerType
import usecases.UseCase

class UpdateProvision(
    unitOfWork: IUnitOfWork,
    private val provisionRepo: IRepository<Provision>,
    private val fundRepo: IRepository<Fund>,
    private val scheduleInvoiceRepo: IRepository<ScheduleInvoice>,
    private val updateScheduleInvoice: IUseCase<UpdateScheduleInvoiceInput, Unit>,
): UseCase<UpdateProvisionInput, Unit>(unitOfWork) {
    override suspend fun process(input: UpdateProvisionInput) {
        val provision = provisionRepo.get(input.id) ?: throw NotFoundException.SingleEntity(input.id, "provision")

        if (input.title != null) {
            if (input.title.equals(provision.title, true) && provisionRepo.existsByName(input.title))
                throw AlreadyExistException.EntitiesByField(mapOf("name" to input.title), "provision")

            provision.title = input.title
        }

        if (input.costHT != null)
            provision.costHT = input.costHT

        if (input.costTTC != null)
            provision.costTTC = input.costTTC

        if (input.expectedLifespanMonth != null)
            provision.expectedLifespanMonth = input.expectedLifespanMonth

        if (input.isPatrimony != null)
            provision.isPatrimony = input.isPatrimony

        if (input.floorValue != null)
            provision.floorValue = input.floorValue

        if (input.acquisitionDate != null)
            provision.acquisitionDate = input.acquisitionDate

        if (input.fundAmortizationId != null)
            provision.fundAmortizationId = input.fundAmortizationId

        if (input.depreciationCriteria != null) {
            val criteriaToAdd = input.depreciationCriteria.filter { criteria -> provision.depreciationCriteria.find { it == criteria } == null }
            val criteriaToRemove = provision.depreciationCriteria.filter { criteria -> input.depreciationCriteria.find { it == criteria } == null }

            val criteria = provision.depreciationCriteria
            criteria.addAll(criteriaToAdd)
            criteria.removeAll(criteriaToRemove)

            provision.depreciationCriteria = criteria
        }

        if (input.interestLoan != null)
            provision.interestLoan = input.interestLoan

        if (input.loanMonth != null && provision.type == ProvisionType.DEPRECIATE_LOAN)
            provision.loanMonth = input.loanMonth.toLong()

        if (input.isInstallmentOnTTC != null)
            provision.isInstallmentOnTTC = input.isInstallmentOnTTC

        val isDepreciateLoan = input.scheduleInvoice != null && provision.type == ProvisionType.DEPRECIATE_LOAN
        // val doUpdateLoan = input.costTTC != null || input.loanMonth != null || input.interestLoan != null || input.scheduleInvoice != null

        if (isDepreciateLoan && input.fundAmortizationId != null) {
            val fund = fundRepo.get(input.fundAmortizationId) ?: throw NotFoundException.SingleEntity(input.fundAmortizationId, "saving_goal")
            if (fund.type != FundType.AMORTIZATION)
                throw ValidationException.YouHaveToSelectOnlyAmortizationFund()
        }

        if (isDepreciateLoan) {
            if (input.loanMonth == null)
                throw UnExpectedException("Unexpected error loanMonth = ${input.loanMonth}")

            val endLoanDate = provision.acquisitionDate.plusMonths(input.loanMonth.toLong())
            val scheduler = Scheduler(
                date = provision.acquisitionDate.atStartOfDay(),
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

        if (provision.hasChanged())
            provisionRepo.update(provision)

        if (provision.type == ProvisionType.DEPRECIATE_LOAN && provision.paymentInfo != null) {
            val scheduleInvoiceCondition = QueryExtendBuilder<ScheduleInvoice>()
                .addCondition("moduleLinker.sourceId", QueryComparator.Equal, provision.id)
                .addCondition("moduleLinker.module", QueryComparator.Equal, ScheduleInvoiceModuleLinkerType.PROVISION.value)

            val scheduleInvoices = scheduleInvoiceRepo.getAll(QueryFilter.queryAll(), scheduleInvoiceCondition)
            if (scheduleInvoices.items.isNotEmpty()) {
                updateScheduleInvoice.processDirect(UpdateScheduleInvoiceInput(
                    id = scheduleInvoices.items.first().id,
                    name = provision.title,
                    amount = provision.paymentInfo!!.paymentAmount,
                    categoryId = provision.paymentInfo!!.categoryId,
                    tagIds = provision.paymentInfo!!.tagIds,
                    schedule = SchedulerInvoiceInput(
                        dueDate = provision.paymentInfo!!.scheduler.date,
                        repeater = provision.paymentInfo!!.scheduler.repeater?.let {
                            ScheduleRepeaterInput(
                                period = it.period,
                                interval = it.interval,
                            )
                        }
                    ),
                    endDate = provision.paymentInfo!!.endDate.atStartOfDay(),
                    passContextEdit = true,
                ))
            }
        }
    }
}