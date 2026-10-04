package usecases.finance_principles

import usecases.UseCase
import adapters.dto.QueryFilter
import adapters.repositories.IRepository
import domain.entities.FinancePrinciple
import usecases.dto.ListOutput
import usecases.finance_principles.dto.GetFinancePrincipleOutput
class GetAllFinancePrinciple(
    val financePrincipleRepo: IRepository<FinancePrinciple>
): UseCase<QueryFilter, ListOutput<GetFinancePrincipleOutput>>() {
    override suspend fun process(input: QueryFilter): ListOutput<GetFinancePrincipleOutput> {
        val financePrincipes = financePrincipleRepo.getAll(input)

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