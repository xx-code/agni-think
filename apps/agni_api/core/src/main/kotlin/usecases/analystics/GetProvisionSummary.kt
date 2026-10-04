package usecases.analystics

import usecases.UseCase
import adapters.dto.QueryFilter
import adapters.repositories.IRepository
import domain.entities.Provision
import usecases.analystics.dto.GetProvisionSummaryOutput
class GetProvisionSummary(
    private val provisionRepo: IRepository<Provision>
): UseCase<Unit, GetProvisionSummaryOutput>() {
    override suspend fun process(input: Unit): GetProvisionSummaryOutput {
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