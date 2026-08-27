package dev.auguste.agni_api.core.usecases.provisionable

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.dto.QuerySortBy
import dev.auguste.agni_api.core.adapters.dto.ScheduleRepeaterOutput
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Provision
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.provisionable.dto.GetProvisionOutput
import dev.auguste.agni_api.core.usecases.provisionable.dto.ProvisionDepreciateCriteriaOutput
import dev.auguste.agni_api.core.usecases.provisionable.dto.ProvisionInvoiceOutput

class GetAllProvisionable(
    private val provisionRepo: IRepository<Provision>
): IUseCase<QueryFilter, ListOutput<GetProvisionOutput>> {
    override fun execAsync(input: QueryFilter): ListOutput<GetProvisionOutput> {
        val query = QueryFilter(
            offset = input.offset,
            limit = input.limit,
            sortBy = QuerySortBy(
                by = "updated_at",
            )
        )
        val provisionables =  provisionRepo.getAll(query)

        return ListOutput(
            items = provisionables.items.map { provisional ->
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
            total = provisionables.total,
        )

    }
}