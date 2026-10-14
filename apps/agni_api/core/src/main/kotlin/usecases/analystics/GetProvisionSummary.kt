package usecases.analystics

import adapters.IAuthenticateAdapter
import usecases.UseCase
import adapters.dto.QueryFilter
import adapters.repositories.IRepository
import adapters.repositories.QueryComparator
import adapters.repositories.QueryExtendBuilder
import domain.entities.Provision
import usecases.analystics.dto.GetProvisionSummaryOutput
class GetProvisionSummary(
    private val provisionRepo: IRepository<Provision>,
    auth: IAuthenticateAdapter
): UseCase<Unit, GetProvisionSummaryOutput>(auth = auth) {
    override suspend fun process(input: Unit): GetProvisionSummaryOutput {
        val currentUser = getCurrentUser()
        val condition = QueryExtendBuilder<Provision>()
            .addCondition("userId", QueryComparator.Equal, currentUser.id)
        val provisions = provisionRepo.getAll(QueryFilter.queryAll(), condition)

        return GetProvisionSummaryOutput(
            activesProvision = provisions.total.toInt(),
            initialValue = provisions.items.sumOf { it.costHT },
            accountingTotalValue = provisions.items.sumOf { it.calculateResidualValue() },
            costByMonth = provisions.items.sumOf { it.calculateTotalCostPerMonth() },
            monthlyPayment = provisions.items.sumOf { it.calculateMonthlyPayment() }
        )
    }
}