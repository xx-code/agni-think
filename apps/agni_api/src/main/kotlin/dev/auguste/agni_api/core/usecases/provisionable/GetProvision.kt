package dev.auguste.agni_api.core.usecases.provisionable

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.DomainException
import dev.auguste.agni_api.core.entities.Provision
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.provisionable.dto.GetProvisionOutput
import dev.auguste.agni_api.core.usecases.provisionable.dto.ProvisionDepreciateCriteriaOutput
import dev.auguste.agni_api.core.usecases.provisionable.dto.ProvisionInvoiceOutput
import java.util.UUID

class GetProvision(
    private val provisionRepo: IRepository<Provision>
    ): IUseCase<UUID, GetProvisionOutput> {
    override fun execAsync(input: UUID): GetProvisionOutput {
        val provisional = provisionRepo.get(input) ?: throw DomainException.NotFound.Provisionable(input)

        return GetProvisionOutput(
            id = provisional.id,
            title = provisional.title,
            costHT = provisional.costHT,
            costTTC = provisional.costTTC,
            acquisitionDate = provisional.acquisitionDate,
            expectedLifespanMonth = provisional.expectedLifespanMonth,
            totalCost = provisional.calculateTotalCost(),
            costByMonth = provisional.calculateTotalCostPerMonth(),
            monthlyPayment = provisional.calculateMonthlyPayment(),
            residualValue = provisional.calculateResidualValue(),
            isPatrimony = provisional.isPatrimony,
            type = provisional.type.value,
            floorValue = provisional.floorValue,
            interestLoan = provisional.interestLoan,
            loanMonth = provisional.loanMonth.toInt(),
            depreciationCriteria = provisional.depreciationCriteria.map {
                ProvisionDepreciateCriteriaOutput(
                    title = it.title,
                    description = it.description,
                    type = it.type.value,
                    value = it.value,
                    monthRange = it.monthRange
                )
            },
            scheduleInvoice = provisional.paymentInfo?.let {
                ProvisionInvoiceOutput(
                    accountId = it.accountId,
                    categoryId = it.categoryId,
                    tagIds = it.tagIds.toList(),
                    budgetIds = it.budgetIds.toList(),
                    nextPaymentDate = it.scheduler.date.toLocalDate(),
                    paymentPeriod = it.scheduler.repeater?.period?.value,
                    paymentInterval = it.scheduler.repeater?.interval
                )
            }
        )
    }
}