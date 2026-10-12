package usecases.finance_principles

import adapters.IAuthenticateAdapter
import usecases.UseCase
import adapters.dto.QueryFilter
import adapters.repositories.IRepository
import adapters.repositories.QueryComparator
import adapters.repositories.QueryExtendBuilder
import domain.entities.FinancePrinciple
import usecases.dto.ListOutput
import usecases.finance_principles.dto.GetAllFinancePrincipleInput
import usecases.finance_principles.dto.GetFinancePrincipleOutput
class GetAllFinancePrinciple(
    val financePrincipleRepo: IRepository<FinancePrinciple>,
    auth: IAuthenticateAdapter
): UseCase<GetAllFinancePrincipleInput, ListOutput<GetFinancePrincipleOutput>>(auth = auth) {
    override suspend fun process(input: GetAllFinancePrincipleInput): ListOutput<GetFinancePrincipleOutput> {
        val currentUser = getCurrentUser()
        val condition = QueryExtendBuilder<FinancePrinciple>()
            .addCondition("userId", QueryComparator.Equal, currentUser.id)
        val financePrincipes = financePrincipleRepo.getAll(input.queryFilter, condition)

        return ListOutput(
            items = financePrincipes.items.map { GetFinancePrincipleOutput(
                id = it.id,
                description = it.description,
                targetType = it.targetType.value,
                logicRules = it.logicRules,
                name = it.name,
                strictness = it.strictness,
            ) },
            total = financePrincipes.total
        )
    }
}