package usecases.provision

import usecases.UseCase
import adapters.dto.QueryFilter
import adapters.dto.QuerySortBy
import adapters.repositories.IRepository
import domain.entities.Provision
import usecases.dto.ListOutput
import usecases.provision.dto.GetProvisionOutput
import usecases.provision.dto.ProvisionDepreciateCriteriaOutput
import usecases.provision.dto.ProvisionInvoiceOutput

class GetAllProvision(
    private val provisionRepo: IRepository<Provision>
): UseCase<QueryFilter, ListOutput<GetProvisionOutput>>() {
    override suspend fun process(input: QueryFilter): ListOutput<GetProvisionOutput> {
        val query = QueryFilter(
            offset = input.offset,
            limit = input.limit,
            sortBy = QuerySortBy(
                by = "updated_at",
            )
        )
        val provisions =  provisionRepo.getAll(query)

        return ListOutput(
            items = provisions.items.map { provisional ->
                GetProvisionOutput(
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
                            paymentInterval = it.scheduler.repeater?.interval,
                        )
                    }
                )
            },
            total = provisions.total,
        )

    }
}