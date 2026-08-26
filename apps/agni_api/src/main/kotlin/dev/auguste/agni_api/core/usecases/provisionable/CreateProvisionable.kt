package dev.auguste.agni_api.core.usecases.provisionable

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.DomainException
import dev.auguste.agni_api.core.entities.Provision
import dev.auguste.agni_api.core.entities.enums.ProvisionType
import dev.auguste.agni_api.core.usecases.CreatedOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.provisionable.dto.CreateProvisionInput
import dev.auguste.agni_api.core.value_objects.ProvisionPayment

class CreateProvisionable(
    private val provisionRepo: IRepository<Provision>
) : IUseCase<CreateProvisionInput, CreatedOutput> {
    override fun execAsync(input: CreateProvisionInput): CreatedOutput {
        if (provisionRepo.existsByName(input.title))
            throw DomainException.AlreadyExist.Provisionable(input.title)

        val provision = Provision(
            title = input.title,
            initialCost = input.initialCost,
            acquisitionDate = input.acquisitionDate,
            expectedLifespanMonth = input.expectedLifespanMonth,
            isPatrimony = input.isPatrimony,
            depreciationCriteria = input.depreciationCriteria.toMutableList(),
            floorValue = input.floorValue,
            type = input.type,
            interestLoan = input.interestLoan,
            loanMonth = input.loanMonth.toLong(),
        )

        if (input.scheduleInvoice != null && input.type == ProvisionType.DEPRECIATE_LOAN) {
            val loanAmount = ProvisionCommon.determineScheduleInvoiceDepreciateLoan(
                initialCost = provision.calculateTotalCost(),
                monthlyPayment = provision.calculateMonthlyPayment(),
                scheduler = input.scheduleInvoice.scheduler
            )

            val payment = ProvisionPayment(
                accountId = input.scheduleInvoice.invoiceAccountId,
                categoryId = input.scheduleInvoice.invoiceCategoryId,
                budgetIds = input.scheduleInvoice.budgetIds,
                tagIds = input.scheduleInvoice.tagIds,
                paymentAmount = loanAmount,
                scheduler = input.scheduleInvoice.scheduler,
                endDate = input.scheduleInvoice.endDate
            )

            provision.paymentInfo = payment
        }

        provisionRepo.create(provision)

        return CreatedOutput(provision.id)
    }
}