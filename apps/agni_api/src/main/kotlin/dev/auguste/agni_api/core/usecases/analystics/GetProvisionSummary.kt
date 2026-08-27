package dev.auguste.agni_api.core.usecases.analystics

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Provision
import dev.auguste.agni_api.core.usecases.analystics.dto.GetProvisionSummaryOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase

class GetProvisionSummary(
    private val provisionRepo: IRepository<Provision>
) : IUseCase<Unit, GetProvisionSummaryOutput> {
    override fun execAsync(input: Unit): GetProvisionSummaryOutput {
        val provisions = provisionRepo.getAll(QueryFilter.queryAll())

        return GetProvisionSummaryOutput(
            activesProvision = provisions.total.toInt(),
            initialValue = provisions.items.sumOf { it.costHT },
            accountingTotalValue = provisions.items.sumOf { it.calculateResidualValue() },
            costByMonth = provisions.items.sumOf { it.calculateTotalCostPerMonth() },
            monthlyPayment = provisions.items.sumOf { it.calculateMonthlyPayment() }
        )
    }
}