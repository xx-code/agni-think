package dev.auguste.agni_api.core.usecases.provisionable

import dev.auguste.agni_api.core.adapters.dto.ScheduleRepeaterInput
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.adapters.repositories.IUnitOfWork
import dev.auguste.agni_api.core.entities.DomainException
import dev.auguste.agni_api.core.entities.Provision
import dev.auguste.agni_api.core.entities.ScheduleInvoice
import dev.auguste.agni_api.core.entities.enums.ProvisionType
import dev.auguste.agni_api.core.usecases.CreatedOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.provisionable.dto.UpdateProvisionInput
import dev.auguste.agni_api.core.usecases.schedule_Invoices.dto.CreateScheduleInvoiceInput
import dev.auguste.agni_api.core.usecases.schedule_Invoices.dto.SchedulerInvoiceInput
import dev.auguste.agni_api.core.usecases.schedule_Invoices.dto.UpdateScheduleInvoiceInput
import dev.auguste.agni_api.core.value_objects.ProvisionPayment
import dev.auguste.agni_api.core.value_objects.Scheduler
import dev.auguste.agni_api.core.value_objects.SchedulerRecurrence
import org.jetbrains.annotations.Async

class UpdateProvisionable(
    private val unitOfWork: IUnitOfWork,
    private val provisionRepo: IRepository<Provision>
): IUseCase<UpdateProvisionInput, Unit> {
    override fun execAsync(input: UpdateProvisionInput) {
        unitOfWork.let {
            val provisionable = provisionRepo.get(input.id) ?: throw DomainException.NotFound.Provisionable(input.id)

            if (input.title != null) {
                if (input.title.equals(provisionable.title, true) && provisionRepo.existsByName(input.title))
                    throw DomainException.AlreadyExist.Provisionable(input.title)

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

            val isDepreciateLoan = input.scheduleInvoice != null && input.type == ProvisionType.DEPRECIATE_LOAN
            val doUpdateLoan = input.costTTC != null || input.loanMonth != null

            if (doUpdateLoan && isDepreciateLoan) {
                if (input.loanMonth == null)
                    throw DomainException.Unexpected.Unknown("Unexpected error loanMonth = ${input.loanMonth}")

                val endLoanDate = provisionable.acquisitionDate.plusMonths(input.loanMonth.toLong())
                val scheduler = Scheduler(
                    date = provisionable.paymentInfo?.scheduler?.date ?: provisionable.acquisitionDate.atStartOfDay(),
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
        }
    }
}