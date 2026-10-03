package usecases.provisionable

import usecases.UseCase
import adapters.repositories.IRepository
import domain.exceptions.NotFoundException
import domain.entities.Provision
import usecases.provisionable.dto.GetProvisionOutput
import usecases.provisionable.dto.ProvisionDepreciateCriteriaOutput
import usecases.provisionable.dto.ProvisionInvoiceOutput
import java.util.UUID

class GetProvision(
    private val provisionRepo: IRepository<Provision>
    ): UseCase<UUID, GetProvisionOutput>() {
    override suspend fun process(input: UUID): GetProvisionOutput {
        val provisional = provisionRepo.get(input) ?: throw NotFoundException.SingleEntity(input, "provisionable")

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
            fundAmortizationId = provisional.fundAmortizationId,
            isInstallmentOnTTC = provisional.isInstallmentOnTTC,
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