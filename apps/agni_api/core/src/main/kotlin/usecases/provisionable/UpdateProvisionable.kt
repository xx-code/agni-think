package usecases.provisionable

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
import usecases.interfaces.IUseCase
import usecases.provisionable.dto.UpdateProvisionInput
import usecases.schedule_Invoices.dto.SchedulerInvoiceInput
import usecases.schedule_Invoices.dto.UpdateScheduleInvoiceInput
import domain.value_objects.ProvisionPayment
import domain.value_objects.Scheduler
import domain.value_objects.SchedulerRecurrence
import domain.enums.FundType
import domain.enums.ProvisionType
import domain.enums.ScheduleInvoiceModuleLinkerType

class UpdateProvisionable(
    private val unitOfWork: IUnitOfWork,
    private val provisionRepo: IRepository<Provision>,
    private val fundRepo: IRepository<Fund>,
    private val updateScheduleInvoice: IUseCase<UpdateScheduleInvoiceInput, Unit>,
    private val scheduleInvoiceRepo: IRepository<ScheduleInvoice>,
): IUseCase<UpdateProvisionInput, Unit> {
    override fun execAsync(input: UpdateProvisionInput) {
        unitOfWork.let {
            val provisionable = provisionRepo.get(input.id) ?: throw NotFoundException.SingleEntity(input.id, "provisionable")

            if (input.title != null) {
                if (input.title.equals(provisionable.title, true) && provisionRepo.existsByName(input.title))
                    throw AlreadyExistException.EntitiesByField(mapOf("name" to input.title), "provisionable")

                provisionable.title = input.title
            }

            if (input.costHT != null)
                provisionable.costHT = input.costHT

            if (input.costTTC != null)
                provisionable.costTTC = input.costTTC

            if (input.expectedLifespanMonth != null)
                provisionable.expectedLifespanMonth = input.expectedLifespanMonth

            if (input.isPatrimony != null)
                provisionable.isPatrimony = input.isPatrimony

            if (input.floorValue != null)
                provisionable.floorValue = input.floorValue

            if (input.acquisitionDate != null)
                provisionable.acquisitionDate = input.acquisitionDate

            if (input.fundAmortizationId != null)
                provisionable.fundAmortizationId = input.fundAmortizationId

            if (input.depreciationCriteria != null) {
                val criteriaToAdd = input.depreciationCriteria.filter { criteria -> provisionable.depreciationCriteria.find { it == criteria } == null }
                val criteriaToRemove = provisionable.depreciationCriteria.filter { criteria -> input.depreciationCriteria.find { it == criteria } == null }

                val criteria = provisionable.depreciationCriteria
                criteria.addAll(criteriaToAdd)
                criteria.removeAll(criteriaToRemove)

                provisionable.depreciationCriteria = criteria
            }

            if (input.interestLoan != null)
                provisionable.interestLoan = input.interestLoan

            if (input.loanMonth != null && provisionable.type == ProvisionType.DEPRECIATE_LOAN)
                provisionable.loanMonth = input.loanMonth.toLong()

            if (input.isInstallmentOnTTC != null)
                provisionable.isInstallmentOnTTC = input.isInstallmentOnTTC

            val isDepreciateLoan = input.scheduleInvoice != null && provisionable.type == ProvisionType.DEPRECIATE_LOAN
            // val doUpdateLoan = input.costTTC != null || input.loanMonth != null || input.interestLoan != null || input.scheduleInvoice != null

            if (isDepreciateLoan && input.fundAmortizationId != null) {
                val fund = fundRepo.get(input.fundAmortizationId) ?: throw NotFoundException.SingleEntity(input.fundAmortizationId, "saving_goal")
                if (fund.type != FundType.AMORTIZATION)
                    throw ValidationException.YouHaveToSelectOnlyAmortizationFund()
            }

            if (isDepreciateLoan) {
                if (input.loanMonth == null)
                    throw UnExpectedException("Unexpected error loanMonth = ${input.loanMonth}")

                val endLoanDate = provisionable.acquisitionDate.plusMonths(input.loanMonth.toLong())
                val scheduler = Scheduler(
                    date = provisionable.acquisitionDate.atStartOfDay(),
                    repeater = SchedulerRecurrence(
                        period = input.scheduleInvoice.paymentPeriod,
                        interval = input.scheduleInvoice.paymentInterval
                    )
                )
                scheduler.date = scheduler.upgradeDate()

                val loanAmount = ProvisionCommon.determineScheduleInvoiceDepreciateLoan(
                    initialCost = provisionable.calculateTotalCost(),
                    monthlyPayment = provisionable.calculateMonthlyPayment(),
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

                provisionable.paymentInfo = payment
            }

            if (provisionable.hasChanged())
                provisionRepo.update(provisionable)

            if (provisionable.type == ProvisionType.DEPRECIATE_LOAN && provisionable.paymentInfo != null) {
                val scheduleInvoiceCondition = QueryExtendBuilder<ScheduleInvoice>()
                    .addCondition("moduleLinker.sourceId", QueryComparator.Equal, provisionable.id)
                    .addCondition("moduleLinker.module", QueryComparator.Equal, ScheduleInvoiceModuleLinkerType.PROVISION.value)

                val scheduleInvoices = scheduleInvoiceRepo.getAll(QueryFilter.queryAll(), scheduleInvoiceCondition)
                if (scheduleInvoices.items.isNotEmpty()) {
                    updateScheduleInvoice.execAsync(UpdateScheduleInvoiceInput(
                        id = scheduleInvoices.items.first().id,
                        name = provisionable.title,
                        amount = provisionable.paymentInfo!!.paymentAmount,
                        categoryId = provisionable.paymentInfo!!.categoryId,
                        tagIds = provisionable.paymentInfo!!.tagIds,
                        schedule = SchedulerInvoiceInput(
                            dueDate = provisionable.paymentInfo!!.scheduler.date,
                            repeater = provisionable.paymentInfo!!.scheduler.repeater?.let {
                                ScheduleRepeaterInput(
                                    period = it.period,
                                    interval = it.interval,
                                )
                            }
                        ),
                        endDate = provisionable.paymentInfo!!.endDate.atStartOfDay(),
                        passContextEdit = true,
                    ))
                }
            }
        }
    }
}